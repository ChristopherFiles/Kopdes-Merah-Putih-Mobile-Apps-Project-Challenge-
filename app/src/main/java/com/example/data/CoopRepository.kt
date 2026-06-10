package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Moshi Request/Response Classes ---
data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)
data class GeminiCandidate(val content: GeminiContent?)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class CoopRepository(private val coopDao: CoopDao) {

    // Cache the Retrofit service
    private val geminiApi: GeminiApi? by lazy {
        try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val okHttp = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .client(okHttp)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            retrofit.create(GeminiApi::class.java)
        } catch (e: Exception) {
            Log.e("CoopRepository", "Failed to initialize Gemini API client: ${e.message}")
            null
        }
    }

    // --- Authentication ---
    suspend fun login(nik: String, pin: String): CoopUser? = withContext(Dispatchers.IO) {
        coopDao.login(nik, pin)
    }

    suspend fun register(nik: String, name: String, pin: String, phone: String, initialWajib: Double, initialSukarela: Double): CoopUser = withContext(Dispatchers.IO) {
        val user = CoopUser(
            nik = nik,
            name = name,
            pin = pin,
            phone = phone,
            role = "ANGGOTA",
            balanceWajib = initialWajib,
            balanceSukarela = initialSukarela
        )
        coopDao.insertUser(user)
        
        // Retrieve the registered user to get assigned ID
        val created = coopDao.login(nik, pin) ?: user
        if (initialWajib > 0) {
            coopDao.insertSavingsTransaction(
                SavingsTransaction(
                    userId = created.id,
                    type = "WAJIB",
                    amount = initialWajib,
                    date = System.currentTimeMillis(),
                    notes = "Simpanan Wajib Aktivasi Pendaftaran"
                )
            )
        }
        if (initialSukarela > 0) {
            coopDao.insertSavingsTransaction(
                SavingsTransaction(
                    userId = created.id,
                    type = "SUKARELA",
                    amount = initialSukarela,
                    date = System.currentTimeMillis(),
                    notes = "Simpanan Sukarela Awal"
                )
            )
        }
        created
    }

    // --- Users ---
    fun getUser(id: Int): Flow<CoopUser?> = coopDao.getUserById(id)
    fun getAllUsers(): Flow<List<CoopUser>> = coopDao.getAllUsers()
    suspend fun updateUser(user: CoopUser) = withContext(Dispatchers.IO) {
        coopDao.updateUser(user)
    }

    // --- Savings & Transactions ---
    fun getSavingsTransactions(userId: Int): Flow<List<SavingsTransaction>> = coopDao.getSavingsTransactions(userId)

    suspend fun addSavings(userId: Int, type: String, amount: Double, notes: String): Boolean = withContext(Dispatchers.IO) {
        val user = coopDao.getUserByIdSync(userId) ?: return@withContext false
        
        if (type == "PENARIKAN") {
            if (user.balanceSukarela < amount) {
                return@withContext false // Insufficient funds in voluntary savings
            }
            val updatedUser = user.copy(balanceSukarela = user.balanceSukarela - amount)
            coopDao.updateUser(updatedUser)
        } else if (type == "SUKARELA") {
            val updatedUser = user.copy(balanceSukarela = user.balanceSukarela + amount)
            coopDao.updateUser(updatedUser)
        } else {
            val updatedUser = user.copy(balanceWajib = user.balanceWajib + amount)
            coopDao.updateUser(updatedUser)
        }

        coopDao.insertSavingsTransaction(
            SavingsTransaction(
                userId = userId,
                type = type,
                amount = amount,
                date = System.currentTimeMillis(),
                notes = notes
            )
        )
        true
    }

    // --- Loans ---
    fun getLoansForUser(userId: Int): Flow<List<LoanApplication>> = coopDao.getLoansForUser(userId)
    fun getAllLoans(): Flow<List<LoanApplication>> = coopDao.getAllLoans()

    suspend fun applyLoan(userId: Int, userName: String, amount: Double, purpose: String, tenureMonths: Int): Boolean = withContext(Dispatchers.IO) {
        val user = coopDao.getUserByIdSync(userId) ?: return@withContext false

        // Rule-based evaluation (Fintech analysis in Indonesian)
        val totalAssets = user.balanceWajib + user.balanceSukarela
        val ratio = if (totalAssets > 0) amount / totalAssets else 10.0
        val serviceFee = amount * 0.01 // 1% admin
        val monthlyPayment = (amount / tenureMonths) + (amount * 0.015) // simple 1.5% interest/month

        val score = when {
            totalAssets <= 0 -> 20
            ratio <= 2.0 -> 85 // Safe (loan is <= 2x savings)
            ratio <= 5.0 -> 70 // Moderate
            ratio <= 10.0 -> 50 // Risky
            else -> 35 // Highly risky
        }

        val analysisText = buildString {
            append("REKOMENDASI: ")
            if (score >= 70) {
                append("LAYAK DISETUJUI. ")
            } else if (score >= 50) {
                append("AKOMODASI BERSYARAT (Butuh penjamin/pengurus review). ")
            } else {
                append("PERLU TINJAUAN KETAT / RESTRUKTURISASI. ")
            }
            append(String.format("Skor KSP: %d/100. Rasio Pinjaman terhadap Simpanan %.1fx. ", score, ratio))
            append(String.format("Total Simpanan saat ini Rp %,.0f. ", totalAssets))
            append("Tujuan pinjaman: '$purpose' ")
            if (purpose.lowercase().contains("modal") || purpose.lowercase().contains("tani") || purpose.lowercase().contains("umkm") || purpose.lowercase().contains("pupuk")) {
                append("(Kategori Produktif - Prioritas Tinggi).")
            } else {
                append("(Kategori Konsumtif - Prioritas Sedang).")
            }
        }

        val loan = LoanApplication(
            userId = userId,
            userName = userName,
            amount = amount,
            purpose = purpose,
            tenureMonths = tenureMonths,
            status = "PENDING",
            date = System.currentTimeMillis(),
            monthlyPayment = monthlyPayment,
            aiRecommendation = analysisText
        )
        coopDao.insertLoan(loan)
        true
    }

    suspend fun approveLoan(loan: LoanApplication) = withContext(Dispatchers.IO) {
        val updated = loan.copy(status = "DISETUJUI")
        coopDao.updateLoan(updated)
        // Disburse loan as supplementary voluntary savings for direct usage, simulator style!
        val user = coopDao.getUserByIdSync(loan.userId)
        if (user != null) {
            val updatedUser = user.copy(balanceSukarela = user.balanceSukarela + loan.amount)
            coopDao.updateUser(updatedUser)
            coopDao.insertSavingsTransaction(
                SavingsTransaction(
                    userId = loan.userId,
                    type = "SUKARELA",
                    amount = loan.amount,
                    date = System.currentTimeMillis(),
                    notes = "Pencairan Pinjaman PinBook #${loan.id}"
                )
            )
        }
    }

    suspend fun rejectLoan(loan: LoanApplication) = withContext(Dispatchers.IO) {
        val updated = loan.copy(status = "DITOLAK")
        coopDao.updateLoan(updated)
    }

    // --- UMKM Products ---
    fun getAllProducts(): Flow<List<UmkmProduct>> = coopDao.getAllProducts()
    suspend fun insertProduct(product: UmkmProduct) = withContext(Dispatchers.IO) {
        coopDao.insertProduct(product)
    }
    suspend fun deleteProduct(id: Int) = withContext(Dispatchers.IO) {
        coopDao.deleteProductById(id)
    }

    // --- Community Chats ---
    fun getAllChats(): Flow<List<ChatMessage>> = coopDao.getAllChats()
    suspend fun sendChatMessage(senderId: Int, senderName: String, senderRole: String, message: String) = withContext(Dispatchers.IO) {
        val chat = ChatMessage(
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        coopDao.insertChatMessage(chat)
    }

    // --- Gemini Interactive Advisory API ---
    suspend fun getGeminiAdvice(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Validate Key is not default placeholder or empty
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.i("CoopRepository", "Gemini API key is unconfigured. Falling back to offline simulator.")
            return@withContext offlineAdvisorResponse(prompt)
        }

        val apiService = geminiApi
        if (apiService == null) {
            return@withContext offlineAdvisorResponse(prompt)
        }

        val reqContents = listOf(
            GeminiContent(
                parts = listOf(
                    GeminiPart(text = "Anda adalah Asisten Virtual Keuangan Koperasi Desa Merah Putih Bogor. Bantu masyarakat desa dengan ramah, informatif, dan bahasa Sunda/Indonesia yang luwes namun profesional. Tetap ringkas (maksimal 3 paragraf). Pertanyaan: $prompt")
                )
            )
        )
        val request = GeminiRequest(contents = reqContents)

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Mohon maaf, sistem tidak menerima jawaban teks dari AI. Silakan coba sesaat lagi."
        } catch (e: Exception) {
            Log.e("CoopRepository", "Error invoking Gemini API: ${e.message}")
            offlineAdvisorResponse(prompt)
        }
    }

    // High quality Indonesian local financial parser for offline-mode
    private fun offlineAdvisorResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("pinjam") || lower.contains("utang") -> {
                "Halo! Mengenai pinjaman di Koperasi Desa Merah Putih Bogor, pastikan Anda aktif menyetor Simpanan Wajib. Plafon ideal Anda adalah 3x dari jumlah total simpanan Anda saat ini. Suku bunga kami adalah bertenor lunak (flat 1.5% saja per bulan) yang jauh lebih ringan daripada pinjol ilegal. Gunakan pinjaman hanya untuk sektor produktif pertanian/UMKM Anda agar berkah!"
            }
            lower.contains("simpan") || lower.contains("tabung") -> {
                "Sampurasun! Menabung di koperasi hukumnya produktif demi ketahanan ekonomi keluarga. Kami memiliki Simpanan Wajib (setiap bulan secara tertib) dan Simpanan Sukarela yang fleksibel ditarik kapan saja saat butuh dana darurat. Setiap akhir tahun pembukuan, Anda juga berhak mendapatkan Sisa Hasil Usaha (SHU) berdasarkan keaktifan simpanan Anda. Mari semangat menabung demi kemakmuran desa!"
            }
            lower.contains("umkm") || lower.contains("jual") || lower.contains("pasar") -> {
                "Koperasi Desa Merah Putih menyediakan fitur 'Marketplace UMKM Desa' agar hasil tani, sayur hidroponik, kerajinan tangan, dan jajanan warga Bogor dapat dibeli langsung oleh sesama anggota tanpa perantara. Daftarkan produk Anda sekarang lewat menu Marketplace, isi deskripsi penawaran, dan cantumkan kontak WA agar lancar jualannya!"
            }
            lower.contains("bogor") || lower.contains("wilayah") -> {
                "Koperasi kami berfokus membantu pengembangan komoditas unggulan wilayah Bogor seperti sayur hidroponik Cisarua, ubi Cilembu, madu hutan Halimun Salak, talas, serta kerajinan bambu Ciapus. Kami menyatukan seluruh wargi Bogor dalam inklusi keuangan digital gotong-royong."
            }
            else -> {
                "Sampurasun! Koperasi Desa Merah Putih - Wilayah Bogor siap mendampingi perencanaan keuangan wargi sekalian lewat kemudahan simpanan wajib/sukarela, pengajuan pinjaman aman tanpa agunan rumit, dan promosi produk pertanian/kerajinan khas desa via Marketplace UMKM. Ada yang bisa kami bantu seputar layanan finansial Koperasi?"
            }
        }
    }
}
