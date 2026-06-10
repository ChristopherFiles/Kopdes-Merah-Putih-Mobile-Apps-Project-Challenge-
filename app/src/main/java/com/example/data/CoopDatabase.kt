package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "users")
data class CoopUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nik: String,
    val name: String,
    val role: String, // "ANGGOTA", "PENGURUS", "ADMIN"
    val pin: String,
    val phone: String,
    val balanceWajib: Double,
    val balanceSukarela: Double
)

@Entity(tableName = "savings_transactions")
data class SavingsTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val type: String, // "WAJIB", "SUKARELA", "PENARIKAN"
    val amount: Double,
    val date: Long,
    val notes: String
)

@Entity(tableName = "loan_applications")
data class LoanApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userName: String,
    val amount: Double,
    val purpose: String,
    val tenureMonths: Int,
    val status: String, // "PENDING", "DISETUJUI", "DITOLAK"
    val date: Long,
    val monthlyPayment: Double,
    val aiRecommendation: String
)

@Entity(tableName = "umkm_products")
data class UmkmProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sellerName: String,
    val price: Double,
    val category: String, // "MAKANAN", "KERAJINAN", "PERTANIAN", "JASA"
    val description: String,
    val phone: String,
    val imageUrl: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val senderName: String,
    val senderRole: String, // "ANGGOTA", "PENGURUS", "ADMIN"
    val message: String,
    val timestamp: Long
)

@Dao
interface CoopDao {
    @Query("SELECT * FROM users WHERE nik = :nik AND pin = :pin LIMIT 1")
    suspend fun login(nik: String, pin: String): CoopUser?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<CoopUser?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: Int): CoopUser?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<CoopUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: CoopUser)

    @Update
    suspend fun updateUser(user: CoopUser)

    // Savings Transactions
    @Query("SELECT * FROM savings_transactions WHERE userId = :userId ORDER BY date DESC")
    fun getSavingsTransactions(userId: Int): Flow<List<SavingsTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsTransaction(tx: SavingsTransaction)

    // Loans
    @Query("SELECT * FROM loan_applications WHERE userId = :userId ORDER BY date DESC")
    fun getLoansForUser(userId: Int): Flow<List<LoanApplication>>

    @Query("SELECT * FROM loan_applications ORDER BY date DESC")
    fun getAllLoans(): Flow<List<LoanApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanApplication)

    @Update
    suspend fun updateLoan(loan: LoanApplication)

    // UMKM Products
    @Query("SELECT * FROM umkm_products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<UmkmProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: UmkmProduct)

    @Query("DELETE FROM umkm_products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    // Chats
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChats(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessage)
}

@Database(
    entities = [
        CoopUser::class,
        SavingsTransaction::class,
        LoanApplication::class,
        UmkmProduct::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CoopDatabase : RoomDatabase() {
    abstract fun coopDao(): CoopDao

    companion object {
        @Volatile
        private var INSTANCE: CoopDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CoopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoopDatabase::class.java,
                    "coop_database"
                )
                .addCallback(CoopDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class CoopDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.coopDao()
                    // Populate initial users
                    val sitia = CoopUser(
                        nik = "2001",
                        name = "Ibu Siti Aminah (Peternak Elit)",
                        pin = "111111",
                        phone = "08569999888",
                        role = "ANGGOTA",
                        balanceWajib = 250000.0,
                        balanceSukarela = 1200000.0
                    )
                    val kanga = CoopUser(
                        nik = "2002",
                        name = "Kang Asep Saepudin (Tani Sukses)",
                        pin = "222222",
                        phone = "087812123434",
                        role = "ANGGOTA",
                        balanceWajib = 150000.0,
                        balanceSukarela = 450000.0
                    )
                    val pengurus = CoopUser(
                        nik = "3001",
                        name = "Bapak H. Mulyadi M.Si.",
                        pin = "333333",
                        phone = "081234567890",
                        role = "PENGURUS",
                        balanceWajib = 1000000.0,
                        balanceSukarela = 5000000.0
                    )
                    val admin = CoopUser(
                        nik = "9999",
                        name = "Admin Pusat Merah Putih",
                        pin = "999999",
                        phone = "081111111111",
                        role = "ADMIN",
                        balanceWajib = 0.0,
                        balanceSukarela = 0.0
                    )

                    dao.insertUser(sitia)
                    dao.insertUser(kanga)
                    dao.insertUser(pengurus)
                    dao.insertUser(admin)

                    // Seed some initial products
                    dao.insertProduct(UmkmProduct(
                        name = "Kripik Singkong Madu Gunung Salak",
                        sellerName = "Ibu Siti Aminah",
                        price = 15000.0,
                        category = "MAKANAN",
                        description = "Keripik singkong renyah organik rasa pedas manis buatan UMKM binaan Ciapus Bogor. Tanpa pengawet buatan.",
                        phone = "08569999888"
                    ))
                    dao.insertProduct(UmkmProduct(
                        name = "Sayur Selada Hidroponik Cisarua",
                        sellerName = "Kang Asep",
                        price = 12000.0,
                        category = "PERTANIAN",
                        description = "Sayur selada segar dipetik langsung dari pondok hidroponik Cisarua saat pesanan diterima. Segar dan bebas pestisida.",
                        phone = "087812123434"
                    ))
                    dao.insertProduct(UmkmProduct(
                        name = "Madu Randu Asli Hutan Halimun",
                        sellerName = "Kelompok Tani Halimun",
                        price = 85000.0,
                        category = "PERTANIAN",
                        description = "Madu murni hasil panen lebah liar Apis Dorsata dari kawasan Taman Nasional Halimun Salak. Sangat baik untuk stamina.",
                        phone = "081234567890"
                    ))
                    dao.insertProduct(UmkmProduct(
                        name = "Sapu Lidi Tangkai Emas Sunda",
                        sellerName = "Karya Kreatif Desa",
                        price = 22000.0,
                        category = "KERAJINAN",
                        description = "Sapu lidi tangkai kokoh anyaman serat alam dari perajin tenun pelepah aren desa Bogor Barat.",
                        phone = "081234567890"
                    ))

                    // Seed primary chat messages
                    val now = System.currentTimeMillis()
                    dao.insertChatMessage(ChatMessage(
                        senderId = 3,
                        senderName = "Bapak H. Mulyadi M.Si.",
                        senderRole = "PENGURUS",
                        message = "Sampurasun wargi Bogor! Selamat datang di chat komunitas digital Koperasi Merah Putih.",
                        timestamp = now - 3600000
                    ))
                    dao.insertChatMessage(ChatMessage(
                        senderId = 1,
                        senderName = "Ibu Siti Aminah",
                        senderRole = "ANGGOTA",
                        message = "Rampes Pak Kades! Hatur nuhun aplikasinya mantap pisan, gampang dipakai nyimpen tabungan.",
                        timestamp = now - 1800000
                    ))
                    dao.insertChatMessage(ChatMessage(
                        senderId = 2,
                        senderName = "Kang Asep",
                        senderRole = "ANGGOTA",
                        message = "Punten Pak, abdi dinten ieu bade ngajukan pinjaman kanggo modal pupuk hidroponik.",
                        timestamp = now - 600000
                    ))

                    // Seed an initial transaction
                    dao.insertSavingsTransaction(SavingsTransaction(
                        userId = 1,
                        type = "WAJIB",
                        amount = 250000.0,
                        date = now - 86400000 * 5,
                        notes = "Simpanan Wajib Aktivasi Akun"
                    ))
                    dao.insertSavingsTransaction(SavingsTransaction(
                        userId = 1,
                        type = "SUKARELA",
                        amount = 1200000.0,
                        date = now - 86400000 * 2,
                        notes = "Hasil penjualan telur puyuh"
                    ))
                    dao.insertSavingsTransaction(SavingsTransaction(
                        userId = 2,
                        type = "WAJIB",
                        amount = 150000.0,
                        date = now - 86400000 * 4,
                        notes = "Simpanan Pokok Anggota"
                    ))
                    dao.insertSavingsTransaction(SavingsTransaction(
                        userId = 2,
                        type = "SUKARELA",
                        amount = 450000.0,
                        date = now - 86400000 * 3,
                        notes = "Sisa penjualan sayur kangkung"
                    ))

                    // Seed a pending loan application for user 2 (Kang Asep)
                    dao.insertLoan(LoanApplication(
                        userId = 2,
                        userName = "Kang Asep Saepudin",
                        amount = 2500000.0,
                        purpose = "Membeli pupuk dan benih selada hidroponik",
                        tenureMonths = 6,
                        status = "PENDING",
                        date = now - 3600000 * 2,
                        monthlyPayment = 450000.0,
                        aiRecommendation = "REKOMENDASI: LAYAK DISETUJUI. Riwayat simpanan aktif, rasio pinjaman/simpanan aman (5.5x), tujuan sektor produktif pertanian pangan lokal Bogor."
                    ))
                }
            }
        }
    }
}
