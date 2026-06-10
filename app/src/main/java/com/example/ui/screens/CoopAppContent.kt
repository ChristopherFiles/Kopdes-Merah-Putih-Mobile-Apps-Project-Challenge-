package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.CoopViewModel
import com.example.viewmodel.Screen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CoopAppContent(viewModel: CoopViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                is Screen.Welcome -> WelcomeScreen(
                    onLoginClick = { 
                        viewModel.clearAuthErrors()
                        viewModel.navigateTo(Screen.Login) 
                    },
                    onRegisterClick = { 
                        viewModel.clearAuthErrors()
                        viewModel.navigateTo(Screen.Register) 
                    }
                )
                is Screen.Login -> LoginScreen(
                    viewModel = viewModel,
                    onBackClick = { viewModel.navigateTo(Screen.Welcome) },
                    onRegisterInstead = { 
                        viewModel.clearAuthErrors()
                        viewModel.navigateTo(Screen.Register) 
                    }
                )
                is Screen.Register -> RegisterScreen(
                    viewModel = viewModel,
                    onBackClick = { viewModel.navigateTo(Screen.Welcome) },
                    onLoginInstead = { 
                        viewModel.clearAuthErrors()
                        viewModel.navigateTo(Screen.Login) 
                    }
                )
                else -> {
                    // All logged-in screens are wrapped in a standard scaffold with navigation
                    DashboardScaffold(viewModel = viewModel) {
                        when (screen) {
                            is Screen.Dashboard -> MainDashboardScreen(viewModel)
                            is Screen.SavingsDetail -> SavingsDetailScreen(viewModel)
                            is Screen.AddSavings -> AddSavingsScreen(viewModel)
                            is Screen.ApplyLoan -> ApplyLoanScreen(viewModel)
                            is Screen.LoanDetail -> LoanStatusScreen(viewModel)
                            is Screen.LoanApprovals -> LoanApprovalsScreen(viewModel)
                            is Screen.Marketplace -> MarketplaceScreen(viewModel)
                            is Screen.AddProduct -> AddProductScreen(viewModel)
                            is Screen.CommunityChat -> CommunityChatScreen(viewModel)
                            is Screen.FinancialEducation -> FinancialEducationScreen(viewModel)
                            is Screen.AiAdvisor -> AiAdvisorScreen(viewModel)
                            is Screen.MembersManagement -> MembersManagementScreen(viewModel)
                            else -> MainDashboardScreen(viewModel)
                        }
                    }
                }
            }
        }

        // Global loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MerahPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memproses Data...",
                            fontWeight = FontWeight.Bold,
                            color = FintechSecondary
                        )
                    }
                }
            }
        }
    }
}

// --- UTILS ---
fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

// --- WELCOME LANDING SCREEN ---
@Composable
fun WelcomeScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MerahPrimary, MerahDark, FintechSecondary)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper section: Brand Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Logo Koperasi",
                    tint = MerahPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "KOPERASI DESA",
                fontWeight = FontWeight.Light,
                color = Color.White,
                fontSize = 18.sp,
                letterSpacing = 4.sp
            )
            Text(
                text = "MERAH PUTIH",
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 28.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = "Wilayah Bogor & Sekitarnya",
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        // Center section: App Mascot / Value Proposition Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f, fill = false),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🇮🇩 Gotong Royong Digital Desa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tabungan Wajib & Sukarela Fleksibel", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pinjaman Aman, Bunga Lunak Tanpa Rentenir", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Promosi Gratis Produk Pertanian & UMKM", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analisis Kelayakan Modal AI Terpadu", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // Bottom section: CTA Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("welcome_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "MASUK KE AKUN",
                    fontWeight = FontWeight.Bold,
                    color = MerahPrimary,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("welcome_register_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(2.dp, Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "DAFTAR ANGGOTA BARU",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Dilindungi oleh Ketahanan Ekonomi Pancasila & Koperasi Indonesia",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(
    viewModel: CoopViewModel,
    onBackClick: () -> Unit,
    onRegisterInstead: () -> Unit
) {
    var nik by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FintechBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, CircleShape)
                .testTag("login_back_button")
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = MerahPrimary)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Wilujeng Sumping!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FintechSecondary
        )
        Text(
            text = "Silakan login menggunakan Nomor Induk Kependudukan (NIK) Anda yang terdaftar pada Koperasi Desa.",
            fontSize = 14.sp,
            color = FintechSlate,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // NIK Input
                OutlinedTextField(
                    value = nik,
                    onValueChange = { if (it.length <= 16) nik = it },
                    label = { Text("Nomor NIK (16 angka)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MerahPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_nik_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Input
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("6-Digit PIN Transaksi") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MerahPrimary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_pin_input"),
                    singleLine = true
                )

                if (loginError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MerahPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = loginError!!,
                            color = MerahPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { viewModel.login(nik, pin) },
                    enabled = nik.length == 4 || nik.length == 16 && pin.length == 6 || pin.isNotEmpty(), // accommodating short simulator NIKs
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("MASUK APLIKASI", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Belum daftar?", color = FintechSlate, fontSize = 14.sp)
            TextButton(onClick = onRegisterInstead) {
                Text("Daftar Sekarang", color = MerahPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        // Help text for village elders
        Card(
            colors = CardDefaults.cardColors(containerColor = MerahLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = "Panduan", tint = MerahPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Bimbingan Masuk: NIK Default uji coba: '2001' (Siti), PIN: '111111' atau NIK: '3001' (H. Mulyadi), PIN: '333333' (Pengurus).",
                    fontSize = 12.sp,
                    color = MerahDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// --- REGISTER SCREEN ---
@Composable
fun RegisterScreen(
    viewModel: CoopViewModel,
    onBackClick: () -> Unit,
    onLoginInstead: () -> Unit
) {
    var nik by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var initialWajib by remember { mutableStateOf("100000") } // Min 100,000 for register
    var initialSukarela by remember { mutableStateOf("50000") }
    val registerError by viewModel.registerError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FintechBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, CircleShape)
                .testTag("register_back_button")
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = MerahPrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Daftar Anggota Baru",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FintechSecondary
        )
        Text(
            text = "Bergabunglah dalam kemakmuran bersama wargi Bogor digital secara mandiri.",
            fontSize = 14.sp,
            color = FintechSlate
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = nik,
                    onValueChange = { if (it.length <= 16) nik = it },
                    label = { Text("Nomor NIK (KTP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("register_nik"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Sesuai KTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("register_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor HP / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("register_phone"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("Buat 6-Digit PIN Keamanan") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("register_pin"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = initialWajib,
                        onValueChange = { initialWajib = it },
                        label = { Text("Simpanan Wajib (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = initialSukarela,
                        onValueChange = { initialSukarela = it },
                        label = { Text("Simpanan Sukarela (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (registerError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = registerError!!, color = MerahPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val wajib = initialWajib.toDoubleOrNull() ?: 0.0
                        val sukarela = initialSukarela.toDoubleOrNull() ?: 0.0
                        viewModel.register(nik, name, pin, phone, wajib, sukarela)
                    },
                    enabled = nik.isNotEmpty() && name.isNotEmpty() && pin.length == 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("register_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DAFTAR ANGGOTA SEKARANG", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sudah jadi anggota?", color = FintechSlate, fontSize = 14.sp)
            TextButton(onClick = onLoginInstead) {
                Text("Login Disini", color = MerahPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// --- DASHBOARD SCAFFOLD WITH ROLE SWITCHING SIMULATOR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScaffold(
    viewModel: CoopViewModel,
    content: @Composable () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val screen by viewModel.currentScreen.collectAsState()
    var showRoleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // The Simulator Tool Banner (strictly for prototype testing!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FintechSecondary)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simulator Mode (${currentUser?.role})",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = { showRoleDialog = true },
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "GANTI ROLE 🛠️",
                            color = MerahPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }

                // Standard App top bar
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MerahPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Koperasi Merah Putih",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = FintechSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        if (screen != Screen.Dashboard) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = FintechSecondary)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Close, contentDescription = "Keluar", tint = MerahPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(FintechBackground)
            ) {
                content()
            }
        }
    )

    // Role Switch Dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Pilih Role Integrasi Uji Coba", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ubah identitas Anda seketika untuk menguji aneka visual & flows koperasi desa:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            viewModel.simulateSwitchRoleTo("ANGGOTA")
                            showRoleDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary)
                    ) {
                        Text("Simulasikan peran: ANGGOTA (Warga Desa)")
                    }

                    Button(
                        onClick = {
                            viewModel.simulateSwitchRoleTo("PENGURUS")
                            showRoleDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechSecondary)
                    ) {
                        Text("Simulasikan peran: PENGURUS (Pemberi Approval)")
                    }

                    Button(
                        onClick = {
                            viewModel.simulateSwitchRoleTo("ADMIN")
                            showRoleDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechSlate)
                    ) {
                        Text("Simulasikan peran: ADMIN (Global Monitor)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// --- MAIN DASHBOARD SCREEN ---
@Composable
fun MainDashboardScreen(viewModel: CoopViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allLoans by viewModel.allLoans.collectAsState()
    
    val pendingLoansCount = remember(allLoans) {
        allLoans.count { it.status == "PENDING" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome & Balance Card Combined (Hero)
        // Styled like: bg-linear-to-br from-red-600 to-red-800 rounded-3xl p-6 shadow-xl relative overflow-hidden
        item {
            val totalWajib = currentUser?.balanceWajib ?: 0.0
            val totalSukarela = currentUser?.balanceSukarela ?: 0.0
            val totalSavings = totalWajib + totalSukarela

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFDC2626), Color(0xFF991B1B), Color(0xFF7F1D1D))
                            )
                        )
                        .padding(24.dp)
                ) {
                    // Accent circular blur effects at top right and bottom right
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 30.dp, y = 30.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // User info and greeting
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sampurasun, Wargi Bogor",
                                    color = Color(0xFFFECACA), // red-200
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = currentUser?.name ?: "Anggota Koperasi",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Circular visual identity profile indicator
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.name?.take(2)?.uppercase() ?: "MP",
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Massive Balance segment
                        Column {
                            Text(
                                text = "TOTAL KEKAYAAN KOOPERATIF",
                                color = Color(0xFFFCA5A5), // red-300
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatRupiah(totalSavings),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Sub-features & Badging
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status tag pill
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF4ADE80), CircleShape) // bright green-400
                                    )
                                    Text(
                                        text = "Aktif • " + (currentUser?.role ?: "ANGGOTA"),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Short phone indicator
                            Text(
                                text = currentUser?.phone ?: "",
                                color = Color(0xFFFECACA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Divider and Subcomponents
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Simpanan Wajib", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatRupiah(totalWajib), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Simpanan Sukarela (Cair)", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatRupiah(totalSukarela), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Compact payment action button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .clickable { viewModel.navigateTo(Screen.AddSavings) }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MerahPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TAMBAH SIMPANAN / BAYAR IURAN", color = MerahPrimary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }

        // Asymmetric Bento Grid Navigation Layout
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MENU DIGITAL KOPERASI BOGOR",
                    fontWeight = FontWeight.ExtraBold,
                    color = FintechSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                // Row 1: Pinjaman (Tall) + Column of Simpanan & Pasar Desa (Squares)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Feature Card on the Left (Pinjaman Cepat) - col-span-2 row-span-2 equivalent
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(240.dp)
                            .clickable { viewModel.navigateTo(Screen.ApplyLoan) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFFEDD5), RoundedCornerShape(16.dp)), // Orange light
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💸", fontSize = 24.sp)
                            }

                            Column {
                                Text(
                                    text = "Pinjaman\nCepat",
                                    fontWeight = FontWeight.Black,
                                    color = FintechSecondary,
                                    fontSize = 18.sp,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bunga rendah 1%",
                                    color = SuccessGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Column of two wide/square cells on the Right
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Simpanan Saya Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.SavingsDetail) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(26.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFDBEAFE), RoundedCornerShape(12.dp)), // Blue light
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📈", fontSize = 18.sp)
                                }
                                Column {
                                    Text(
                                        text = "Simpanan",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FintechSecondary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Cek riwayat saldo",
                                        color = FintechSlate,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Pasar Desa Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.Marketplace) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(26.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFD1FAE5), RoundedCornerShape(12.dp)), // Green light
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🧺", fontSize = 18.sp)
                                }
                                Column {
                                    Text(
                                        text = "Pasar UMKM",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FintechSecondary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Beli hasil bumi",
                                        color = FintechSlate,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: QR Code (Small Square) + Edukasi (Small Square) + Chat Warga (Wide Card)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // QR Scan - Red-themed small square
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clickable {
                                Toast.makeText(viewModel.getApplication(), "Simulator QR Aktif: Sukses Mendeteksi Rumah Pembayaran Koperasi!", Toast.LENGTH_LONG).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(26.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, MerahPrimary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("QR", fontWeight = FontWeight.Black, color = MerahPrimary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bayar QR", fontWeight = FontWeight.Bold, color = MerahPrimary, fontSize = 11.sp)
                        }
                    }

                    // Edukasi Tani - Purple/Indigo themed small square
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clickable { viewModel.navigateTo(Screen.FinancialEducation) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                        shape = RoundedCornerShape(26.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎓", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Edukasi", fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), fontSize = 11.sp)
                        }
                    }

                    // Chat Warga - Gold/Amber-themed wide card (weight 2f)
                    Card(
                        modifier = Modifier
                            .weight(2f)
                            .height(115.dp)
                            .clickable { viewModel.navigateTo(Screen.CommunityChat) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(26.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💬", fontSize = 16.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Warga", color = SuccessGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column {
                                Text("Chat Warga", fontWeight = FontWeight.ExtraBold, color = FintechSecondary, fontSize = 14.sp)
                                Text("Komunitas desa", color = FintechSlate, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 3: Asisten AI KSP (Deep dark slate card - matches bg-slate-900 / FintechSecondary)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { viewModel.navigateTo(Screen.AiAdvisor) },
                    colors = CardDefaults.cardColors(containerColor = FintechSecondary),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Backdrop circle design
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 70.dp))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🤖", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Asisten AI KSP", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE0F2FE), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Saku AI", color = Color(0xFF0369A1), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    Text("Tanya analisis & kelayakan pinjaman modal", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // Special Section for PENGURUS or ADMIN: Approval of Loans & Managing Members
                if (currentUser?.role == "PENGURUS" || currentUser?.role == "ADMIN") {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "MENU KHUSUS " + currentUser?.role,
                        fontWeight = FontWeight.ExtraBold,
                        color = MerahPrimary,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Approval management bento item
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(115.dp)
                                .clickable { viewModel.navigateTo(Screen.LoanApprovals) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(26.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(WaitingAmber.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = WaitingAmber, modifier = Modifier.size(18.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(WaitingAmber, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("$pendingLoansCount Baru", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Column {
                                    Text("Persetujuan", fontWeight = FontWeight.Bold, color = FintechSecondary, fontSize = 13.sp)
                                    Text("Seleksi aplikasi modal", color = FintechSlate, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }

                        // Members management bento item
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(115.dp)
                                .clickable { viewModel.navigateTo(Screen.MembersManagement) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(26.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(FintechSecondary.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = FintechSecondary, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text("Buku Anggota", fontWeight = FontWeight.Bold, color = FintechSecondary, fontSize = 13.sp)
                                    Text("Kelola data wargi", color = FintechSlate, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Announcement Banner
        // Matches: bg-white rounded-2xl p-4 border-l-4 border-red-600 shadow-xs flex items-center gap-4
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Custom left border effect matching tailwind: border-l-4 border-red-600
                            drawRect(
                                color = MerahPrimary,
                                size = size.copy(width = 4.dp.toPx())
                            )
                        }
                        .padding(start = 18.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PENGUMUMAN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MerahPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rapat Anggota Tahunan (RAT) Wilayah Bojong Gede tgl 20 Okt.",
                            fontSize = 12.sp,
                            color = FintechSecondary,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("📢", fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun MenuGridItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = FintechSecondary, fontSize = 13.sp, maxLines = 1)
                Text(subtitle, color = FintechSlate, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// --- SAVINGS DETAIL SCREEN ---
@Composable
fun SavingsDetailScreen(viewModel: CoopViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.savingsTransactions.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BUKU TABUNGAN DIGITAL", fontWeight = FontWeight.Bold, color = FintechSlate, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Simpanan Wajib", fontSize = 12.sp, color = FintechSlate)
                        Text(formatRupiah(currentUser?.balanceWajib ?: 0.0), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Simpanan Sukarela", fontSize = 12.sp, color = FintechSlate)
                        Text(formatRupiah(currentUser?.balanceSukarela ?: 0.0), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.navigateTo(Screen.AddSavings) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary)
                ) {
                    Text("SETOR TABUNGAN SEKARANG")
                }
            }
        }

        Text(
            text = "RIWAYAT TRANSAKSI ANDA",
            fontWeight = FontWeight.Bold,
            color = FintechSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = FintechSlate.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada riwayat setoran.", color = FintechSlate)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (tx.type == "PENARIKAN") MerahLight else SuccessGreen.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.type == "PENARIKAN") Icons.Default.Close else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (tx.type == "PENARIKAN") MerahPrimary else SuccessGreen
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = when(tx.type) {
                                            "WAJIB" -> "Setoran Simpanan Wajib"
                                            "SUKARELA" -> "Setoran Simpanan Sukarela"
                                            "PENARIKAN" -> "Penarikan Tabungan"
                                            else -> tx.type
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = FintechSecondary,
                                        fontSize = 14.sp
                                    )
                                    Text(tx.notes, fontSize = 12.sp, color = FintechSlate)
                                    Text(formatDate(tx.date), fontSize = 10.sp, color = FintechSlate.copy(alpha = 0.8f))
                                }
                            }
                            Text(
                                text = (if (tx.type == "PENARIKAN") "-" else "+") + formatRupiah(tx.amount),
                                fontWeight = FontWeight.Black,
                                color = if (tx.type == "PENARIKAN") MerahPrimary else SuccessGreen,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- ADD SAVINGS SCREEN ---
@Composable
fun AddSavingsScreen(viewModel: CoopViewModel) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("SUKARELA") } // WAJIB, SUKARELA, PENARIKAN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("SETOR ATAU TARIK DANA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
        Text("Pilih produk simpanan, nominal, dan isi catatan transaksi Anda.", fontSize = 14.sp, color = FintechSlate)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("PILIH JENIS TRANSAKSI", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FintechSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sukarela Button
                    Button(
                        onClick = { selectedType = "SUKARELA" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "SUKARELA") SuccessGreen else Color.LightGray.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("Sukarela", color = if (selectedType == "SUKARELA") Color.White else FintechSecondary, fontSize = 12.sp)
                    }

                    // Wajib Button
                    Button(
                        onClick = { selectedType = "WAJIB" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "WAJIB") MerahPrimary else Color.LightGray.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("Wajib", color = if (selectedType == "WAJIB") Color.White else FintechSecondary, fontSize = 12.sp)
                    }

                    // Tarik Button
                    Button(
                        onClick = { selectedType = "PENARIKAN" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "PENARIKAN") FintechSecondary else Color.LightGray.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("Tarik", color = if (selectedType == "PENARIKAN") Color.White else FintechSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Nominal Uang (Rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan") },
                    placeholder = { Text("misal: Hasil panen bayam liar") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt <= 0) {
                            Toast.makeText(context, "Masukkan nominal tabungan yang valid!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addSavings(selectedType, amt, notes.ifBlank { "Transaksi Koperasi Desa" }) { success ->
                            if (success) {
                                Toast.makeText(context, "Transaksi Berhasil Dicatat!", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(Screen.Dashboard)
                            } else {
                                Toast.makeText(context, "Transaksi Gagal. Cek kecukupan saldo untuk penarikan!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when(selectedType) {
                            "SUKARELA" -> SuccessGreen
                            "WAJIB" -> MerahPrimary
                            else -> FintechSecondary
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("KONFIRMASI TRANSAKSI", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- APPLY LOAN & AI ADVICE ACTION ---
@Composable
fun ApplyLoanScreen(viewModel: CoopViewModel) {
    val context = LocalContext.current
    val userLoans by viewModel.userLoans.collectAsState()
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf(6) } // Default 6 months

    val monthlyEstimate = remember(amount, tenure) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        if (amt > 0) {
            val base = amt / tenure
            val interest = amt * 0.015 // 1.5% interest
            base + interest
        } else 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("PENGAJUAN PINJAMAN MODAL", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
        Text("Gunakan modal untuk kebutuhan bertani, beternak, atau usaha mikro produktif.", fontSize = 14.sp, color = FintechSlate)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Jumlah Pinjaman (Rp)") },
                    placeholder = { Text("Contoh: 3000000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Tujuan Keperluan Modal") },
                    placeholder = { Text("misal: Pembelian jaring kolam ikan lele Ciapus") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("JANGKA WAKTU PENGEMBALIAN", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FintechSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 6, 12, 24).forEach { months ->
                        Button(
                            onClick = { tenure = months },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tenure == months) MerahPrimary else Color.LightGray.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = "$months Bln",
                                color = if (tenure == months) Color.White else FintechSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Billing estimate details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MerahLight.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimasi Angsuran / Bulan:", fontSize = 13.sp, color = FintechSlate)
                            Text(formatRupiah(monthlyEstimate), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MerahPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bunga flat koperasi: 1.5% per bulan. Bebas biaya tersembunyi.",
                            fontSize = 11.sp,
                            color = MerahDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt <= 0 || purpose.isBlank()) {
                            Toast.makeText(context, "Jumlah pinjaman dan tujuan wajib diisi!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.applyLoan(amt, purpose, tenure) { success ->
                            if (success) {
                                Toast.makeText(context, "Pengajuan Terkirim! AI sedang mengevaluasi skor Anda.", Toast.LENGTH_LONG).show()
                                viewModel.navigateTo(Screen.LoanDetail)
                            } else {
                                Toast.makeText(context, "Proses pengajuan gagal.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("KIRIM PENGAJUAN MODAL", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { viewModel.navigateTo(Screen.LoanDetail) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Lihat Status Pinjaman Sebelumnya", color = MerahPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

// --- LOAN STATUS DETAIL SCREEN ---
@Composable
fun LoanStatusScreen(viewModel: CoopViewModel) {
    val userLoans by viewModel.userLoans.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "DAFTAR PENGAJUAN PINJAMAN ANDA",
            fontWeight = FontWeight.Bold,
            color = FintechSecondary,
            fontSize = 15.sp,
            modifier = Modifier.padding(16.dp)
        )

        if (userLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = FintechSlate.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada pengajuan pinjaman.", color = FintechSlate)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.ApplyLoan) },
                        colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary)
                    ) {
                        Text("Ajukan Pinjaman Sekarang")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(userLoans) { loan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MerahLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatRupiah(loan.amount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = FintechSecondary
                                )
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when(loan.status) {
                                            "DISETUJUI" -> SuccessGreen.copy(alpha = 0.15f)
                                            "DITOLAK" -> MerahLight
                                            else -> WaitingAmber.copy(alpha = 0.15f)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = loan.status,
                                        fontWeight = FontWeight.Bold,
                                        color = when(loan.status) {
                                            "DISETUJUI" -> SuccessGreen
                                            "DITOLAK" -> MerahPrimary
                                            else -> WaitingAmber
                                        },
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tujuan: ${loan.purpose}", fontSize = 13.sp, color = FintechSlate)
                            Text("Tenor: ${loan.tenureMonths} Bulan | Angsuran: ${formatRupiah(loan.monthlyPayment)} /bln", fontSize = 12.sp, color = FintechSlate)
                            Text("Tanggal: ${formatDate(loan.date)}", fontSize = 11.sp, color = FintechSlate.copy(alpha = 0.7f))

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Display AI/KSP evaluation recommendation
                            Card(
                                colors = CardDefaults.cardColors(containerColor = FintechBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp)) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MerahPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = loan.aiRecommendation,
                                        fontSize = 11.sp,
                                        color = FintechSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- LOAN APPROVALS SCREEN (For Pengurus/Admin) ---
@Composable
fun LoanApprovalsScreen(viewModel: CoopViewModel) {
    val allLoans by viewModel.allLoans.collectAsState()
    val pendingLoans = remember(allLoans) {
        allLoans.filter { it.status == "PENDING" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "MODUL PERSETUJUAN KREDIT DESA",
            fontWeight = FontWeight.Bold,
            color = FintechSecondary,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )

        if (pendingLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(48.dp), tint = SuccessGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Semua berkas aman! Belum ada pengajuan baru.", color = FintechSlate)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingLoans) { loan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = BorderStroke(1.dp, MerahLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(loan.userName, fontWeight = FontWeight.Bold, color = FintechSecondary, fontSize = 15.sp)
                                    Text("NIK Pemohon: #${loan.userId}", fontSize = 12.sp, color = FintechSlate)
                                }
                                Text(
                                    text = formatRupiah(loan.amount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MerahPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tujuan Pinjaman: '${loan.purpose}'", fontSize = 13.sp, color = FintechSecondary)
                            Text("Kecepatan Tenor: ${loan.tenureMonths} Bulan | Angsuran: ${formatRupiah(loan.monthlyPayment)} / bln", fontSize = 12.sp, color = FintechSlate)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            // AI Score card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MerahLight.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp)) {
                                    Icon(Icons.Default.Star, contentDescription = "AI", tint = MerahPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = loan.aiRecommendation,
                                        fontSize = 11.sp,
                                        color = MerahDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.rejectLoan(loan) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = FintechSlate),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("TOLAK", color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.approveLoan(loan) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("DISETUJUI 👍", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MARKETPLACE UMKM SCREEN (Beautiful Product list) ---
@Composable
fun MarketplaceScreen(viewModel: CoopViewModel) {
    val products by viewModel.allProducts.collectAsState()
    var selectedCategory by remember { mutableStateOf("SEMUA") }

    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "SEMUA") products
        else products.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PASAR UMKM DESA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
                Text("Beli dari tetangga, majukan ketahanan desa.", fontSize = 12.sp, color = FintechSlate)
            }
            Button(
                onClick = { viewModel.navigateTo(Screen.AddProduct) },
                colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Jual Produk", fontSize = 12.sp)
            }
        }

        // Category Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("SEMUA", "MAKANAN", "PERTANIAN", "KERAJINAN").forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp), tint = FintechSlate.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada pedagang di kategori ini.", color = FintechSlate)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProducts) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            // Category Badge Canvas/Icon
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(MerahLight, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when(item.category) {
                                        "MAKANAN" -> Icons.Default.Star
                                        "PERTANIAN" -> Icons.Default.Home
                                        else -> Icons.Default.ShoppingCart
                                    },
                                    contentDescription = null,
                                    tint = MerahPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = FintechBackground)
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FintechSlate,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechSecondary,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Oleh: ${item.sellerName}",
                                    fontSize = 11.sp,
                                    color = FintechSlate
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    color = FintechSlate,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatRupiah(item.price),
                                        fontWeight = FontWeight.Black,
                                        color = MerahPrimary,
                                        fontSize = 15.sp
                                    )
                                    Button(
                                        onClick = {
                                            // Mock buying / call WA seller
                                            Toast.makeText(viewModel.getApplication(), "Simulator: Menghubungi WhatsApp ${item.sellerName} di ${item.phone}", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("BELI / WA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ADD UMKM PRODUCT FORM ---
@Composable
fun AddProductScreen(viewModel: CoopViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("MAKANAN") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("JUAL PRODUK UMKM ANDA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
        Text("Isi data produk di bawah untuk diiklankan gratis secara nasional untuk seluruh wargi Koperasi.", fontSize = 14.sp, color = FintechSlate)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Dagangan / Produk") },
                    placeholder = { Text("misal: Ubi Madu Cilembu Organik") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga Produk (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("KATEGORI KHAS PRODUK", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FintechSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("MAKANAN", "PERTANIAN", "KERAJINAN").forEach { cat ->
                        Button(
                            onClick = { category = cat },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (category == cat) MerahPrimary else Color.LightGray.copy(alpha = 0.2f)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (category == cat) Color.White else FintechSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Penjelasan Singkat Produk") },
                    placeholder = { Text("Jelaskan keistimewaan produk Anda disini.") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor Kontak HP / WhatsApp") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        val prc = price.toDoubleOrNull() ?: 0.0
                        if (name.isBlank() || prc <= 0 || description.isBlank()) {
                            Toast.makeText(context, "Semua kolom dagangan wajib diisi!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.uploadProduct(name, prc, category, description, phone) {
                            Toast.makeText(context, "Produk Dagangan Berhasil Diunggah!", Toast.LENGTH_LONG).show()
                            viewModel.navigateTo(Screen.Marketplace)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MerahPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("UNGAH KE PASAR UMKM", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- COMMUNITY CHAT SCREEN ---
@Composable
fun CommunityChatScreen(viewModel: CoopViewModel) {
    val chats by viewModel.allChats.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var chatText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(MerahLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = MerahPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("MUSYAWARAH DIGITAL WARGI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
                Text("Saling bertegur sapa sekampung Bogor.", fontSize = 11.sp, color = FintechSlate)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(chats) { chat ->
                val isMe = chat.senderId == currentUser?.id
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) MerahPrimary else Color.White
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        ),
                        border = if (isMe) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isMe) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = chat.senderName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = MerahPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = FintechBackground)
                                    ) {
                                        Text(
                                            text = chat.senderRole,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FintechSlate,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = chat.message,
                                fontSize = 13.sp,
                                color = if (isMe) Color.White else FintechSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date(chat.timestamp)),
                                fontSize = 9.sp,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else FintechSlate.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Chat Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatText,
                onValueChange = { chatText = it },
                placeholder = { Text("Ketik pesan musyawarah...", fontSize = 12.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (chatText.isNotBlank()) {
                        viewModel.sendChat(chatText)
                        chatText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MerahPrimary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
            }
        }
    }
}

// --- FINANCIAL EDUCATION ARTICLES LIST ---
@Composable
fun FinancialEducationScreen(viewModel: CoopViewModel) {
    val articles = remember {
        listOf(
            Triple(
                "Mengenal Sisa Hasil Usaha (SHU) Koperasi",
                "SHU merupakan keuntungan bersih koperasi desa yang diperoleh selama satu tahun buku. Di akhir pembukuan tahunan, SHU dibagikan langsung kembali secara transparan kepada seluruh anggota secara adil berdasarkan keaktifan simpanan dan pinjaman Anda di koperasi. Semakin tertib menabung, semakin besar SHU yang dinikmati wargi di penghujung tahun!",
                "Inklusi Keuangan"
            ),
            Triple(
                "Strategi Budidaya Hidroponik Sayur Premium di Cisarua",
                "Hidroponik sayur selada atau pakcoy memiliki pasar komoditi yang bernilai tinggi dan stabil di kota Bogor. Memanfaatkan pinjaman modal produktif Koperasi Merah Putih berkisar Rp 3.000.000, petani dapat memasang atap UV kecil, membeli nutrisi AB Mix, dan benih unggul. ROI budidaya berkisar 3 bulan dengan panen berkelanjutan.",
                "Sektor Tani Bogor"
            ),
            Triple(
                "Melindungi Diri dari Bahaya Rentenir & Pinjol Ilegal",
                "Rentenir berkokoh mengatasnamakan pinjaman cepat padahal mematok bunga mencekik hingga 30% per minggu. Jauhi penawaran mencurigakan di HP/WA! Koperasi Desa Merah Putih diawasi ketat, berbadan hukum sah, mengusung suku bunga simpanan pinjaman lunak, dan bertujuan murni memajukan kesejahteraan wargi melalui simpanan gotong royong.",
                "Edukasi Finansial"
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("PANDUAN EKONOMI DESA MANDIRI", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
            Text("Edukasi dan bimbingan literasi finansial terpercaya khas masyarakat Bogor.", fontSize = 12.sp, color = FintechSlate)
        }

        items(articles) { article ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MerahLight)
                    ) {
                        Text(
                            text = article.third,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerahDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.first,
                        fontWeight = FontWeight.Bold,
                        color = FintechSecondary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.second,
                        fontSize = 13.sp,
                        color = FintechSlate,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// --- INTELLIGENT AI FINANCIAL CONSULTING SCREEN ---
@Composable
fun AiAdvisorScreen(viewModel: CoopViewModel) {
    val aiMessages by viewModel.aiChatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(MerahLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MerahPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("ASISTEN KEUANGAN AI KSP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
                Text("Kecerdasan buatan penasihat modal usaha khas wargi Bogor.", fontSize = 11.sp, color = FintechSlate)
            }
            TextButton(onClick = { viewModel.clearAiChat() }) {
                Text("Hapus", color = MerahPrimary, fontSize = 12.sp)
            }
        }

        // Conversational Scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(aiMessages) { msg ->
                val isUser = msg.second
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 290.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) FintechSecondary else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isUser) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "Wargi (Saya)" else "🤖 ASISTEN AI KUMPUL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isUser) Color.White.copy(alpha = 0.8f) else MerahPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.first,
                                fontSize = 13.sp,
                                color = if (isUser) Color.White else FintechSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Card(
                        modifier = Modifier.width(120.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = MerahPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI berpikir...", fontSize = 11.sp, color = FintechSlate)
                        }
                    }
                }
            }
        }

        // Bottom Input query
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Tanyakan rekomendasi modal...", fontSize = 12.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        viewModel.askAiAdvisor(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MerahPrimary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
            }
        }
    }
}

// --- MEMBERS MANAGEMENT SCREEN (Admin panel) ---
@Composable
fun MembersManagementScreen(viewModel: CoopViewModel) {
    val users by viewModel.allUsers.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "BUKU DATA BESAR ANGGOTA KOPERASI",
            fontWeight = FontWeight.Bold,
            color = FintechSecondary,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(member.name, fontWeight = FontWeight.Bold, color = FintechSecondary, fontSize = 14.sp)
                                Text("NIK: ${member.nik} | HP: ${member.phone}", fontSize = 11.sp, color = FintechSlate)
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (member.role == "ANGGOTA") SuccessGreen.copy(alpha = 0.15f) else MerahLight
                                )
                            ) {
                                Text(
                                    text = member.role,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (member.role == "ANGGOTA") SuccessGreen else MerahPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Simpanan Wajib:", fontSize = 10.sp, color = FintechSlate)
                                Text(formatRupiah(member.balanceWajib), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Simpanan Sukarela:", fontSize = 10.sp, color = FintechSlate)
                                Text(formatRupiah(member.balanceSukarela), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
