package com.example.gestionsolicitudes.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UsuarioEntity::class, SolicitudEntity::class, EventoHistorialEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        private fun sembrarUsuarios(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT OR IGNORE INTO usuarios VALUES ('U1','Camila Cliente','cliente@demo.cl','123456','CLIENTE','Pyme Demo SpA')")
            db.execSQL("INSERT OR IGNORE INTO usuarios VALUES ('U2','Andrés Admin','admin@demo.cl','123456','ADMINISTRADOR','Servicios Digitales Demo')")
            db.execSQL("INSERT OR IGNORE INTO usuarios VALUES ('U3','Tomás Técnico','tecnico@demo.cl','123456','TECNICO','Servicios Digitales Demo')")
            db.execSQL("INSERT OR IGNORE INTO usuarios VALUES ('U4','Valentina Soporte','tecnico2@demo.cl','123456','TECNICO','Servicios Digitales Demo')")
        }

        fun getDatabase(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val appContext = context.applicationContext
                recrearSiCambioElEsquema(appContext)
                instancia ?: Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    NOMBRE_DB
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            sembrarUsuarios(db)
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            sembrarUsuarios(db)
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
        }

        private fun recrearSiCambioElEsquema(context: Context) {
            val archivo = context.getDatabasePath(NOMBRE_DB)
            if (!archivo.exists()) return

            val hashGuardado = hashEnArchivo(archivo.absolutePath)
            val hashActual = hashDelEsquemaActual(context)
            if (hashGuardado == null || hashActual == null || hashGuardado != hashActual) {
                context.deleteDatabase(NOMBRE_DB)
            }
        }

        private fun hashDelEsquemaActual(context: Context): String? {
            context.deleteDatabase(NOMBRE_DB_TEMPORAL)
            val temporal = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                NOMBRE_DB_TEMPORAL
            ).build()
            return try {
                val cursor = temporal.openHelper.writableDatabase.query(
                    "SELECT identity_hash FROM room_master_table WHERE id = 42"
                )
                cursor.use { if (it.moveToFirst()) it.getString(0) else null }
            } catch (_: Exception) {
                null
            } finally {
                temporal.close()
                context.deleteDatabase(NOMBRE_DB_TEMPORAL)
            }
        }

        private fun hashEnArchivo(ruta: String): String? {
            return try {
                SQLiteDatabase.openDatabase(ruta, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    db.rawQuery(
                        "SELECT identity_hash FROM room_master_table WHERE id = 42",
                        null
                    ).use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                }
            } catch (_: Exception) {
                null
            }
        }

        private const val NOMBRE_DB = "gestion_solicitudes.db"
        private const val NOMBRE_DB_TEMPORAL = "gestion_solicitudes_esquema.db"
    }
}
