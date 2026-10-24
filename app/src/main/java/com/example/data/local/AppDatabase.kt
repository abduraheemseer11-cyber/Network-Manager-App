package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.NetworkDeviceDao
import com.example.data.local.dao.RouterDao
import com.example.data.local.dao.VoucherDao
import com.example.data.local.entity.GeneratedVoucher
import com.example.data.local.entity.NetworkDevice
import com.example.data.local.entity.RouterConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RouterConfig::class, GeneratedVoucher::class, NetworkDevice::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routerDao(): RouterDao
    abstract fun voucherDao(): VoucherDao
    abstract fun networkDeviceDao(): NetworkDeviceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mikhmon_network_manager.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            // Seed default router
                            database.routerDao().insertRouter(
                                RouterConfig(
                                    name = "راوتر الهوتسبوت الرئيسي",
                                    ipAddress = "192.168.88.1",
                                    apiPort = 8728,
                                    username = "admin",
                                    password = "",
                                    hotspotName = "شبكة ميكروتك الذكية",
                                    dnsName = "wifi.login",
                                    currency = "ر.ي",
                                    isActive = true
                                )
                            )
                            // Seed default network devices for map visualization
                            database.networkDeviceDao().insertDevices(
                                listOf(
                                    NetworkDevice(
                                        name = "برج الإرسال الشمالي (Base 1)",
                                        type = "SECTOR_ANTENNA",
                                        latitude = 24.7136,
                                        longitude = 46.6753,
                                        status = "ONLINE",
                                        ipAddress = "192.168.88.10",
                                        connectedClients = 48,
                                        notes = "تغطية قطاعية بزاوية 120 درجة"
                                    ),
                                    NetworkDevice(
                                        name = "نقطة توزيع الحي الغربي (AP 2)",
                                        type = "ACCESS_POINT",
                                        latitude = 24.7180,
                                        longitude = 46.6810,
                                        status = "ONLINE",
                                        ipAddress = "192.168.88.11",
                                        connectedClients = 32,
                                        notes = "جهاز UniFi LR"
                                    ),
                                    NetworkDevice(
                                        name = "سويتش التوزيع المركزي (Switch Core)",
                                        type = "SWITCH",
                                        latitude = 24.7100,
                                        longitude = 46.6710,
                                        status = "ONLINE",
                                        ipAddress = "192.168.88.2",
                                        connectedClients = 112,
                                        notes = "MikroTik Cloud Router Switch CRS326"
                                    ),
                                    NetworkDevice(
                                        name = "محطة الربط اللاسلكي (Point to Point)",
                                        type = "ROUTER",
                                        latitude = 24.7230,
                                        longitude = 46.6690,
                                        status = "WARNING",
                                        ipAddress = "192.168.88.20",
                                        connectedClients = 19,
                                        notes = "إشارة منخفضة نسبياً -72dBm"
                                    )
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
