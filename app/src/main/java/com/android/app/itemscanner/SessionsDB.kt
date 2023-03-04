package com.android.app.itemscanner

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns
import androidx.core.database.getStringOrNull
import com.android.app.itemscanner.api.ScanSession
import java.util.Date

class SessionsDB(context: Context) {

    private val dbHelper: SessionDbHelper

    init {
        this.dbHelper = SessionDbHelper(context)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "FeedReader.db"
        private const val DATABASE_TABLE = "SessionsTable"

        // Table contents are grouped together in an anonymous object.
        object SessionEntry : BaseColumns {
            const val COLUMN_TITLE = "title"
            const val COLUMN_NUM_PHOTOS = "num_photos"
            const val COLUMN_CREATION_TIME = "creation_time"
            const val COLUMN_IMAGE = "image"
            const val COLUMN_ZIP_FILE = "zip_file"
        }

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE $DATABASE_TABLE (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${SessionEntry.COLUMN_CREATION_TIME} REAL," +
                    "${SessionEntry.COLUMN_TITLE} TEXT," +
                    "${SessionEntry.COLUMN_NUM_PHOTOS} INTEGER," +
                    "${SessionEntry.COLUMN_IMAGE} TEXT," +
                    "${SessionEntry.COLUMN_ZIP_FILE} TEXT)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $DATABASE_TABLE"
    }

    fun insert(scanSession: ScanSession) {
        val values = ContentValues().apply {
            put(SessionEntry.COLUMN_CREATION_TIME, scanSession.creationTime.time)
            put(SessionEntry.COLUMN_TITLE, scanSession.title)
            put(SessionEntry.COLUMN_NUM_PHOTOS, scanSession.numPhotos)
            put(SessionEntry.COLUMN_IMAGE, scanSession.image?.path)
            put(SessionEntry.COLUMN_ZIP_FILE, scanSession.zipFile?.path)
        }
        dbHelper.writableDatabase.insert(DATABASE_TABLE, null, values)
    }

    fun getSessions(): List<ScanSession> {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            BaseColumns._ID,
            SessionEntry.COLUMN_CREATION_TIME,
            SessionEntry.COLUMN_TITLE,
            SessionEntry.COLUMN_NUM_PHOTOS,
            SessionEntry.COLUMN_IMAGE,
            SessionEntry.COLUMN_ZIP_FILE
        )

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${SessionEntry.COLUMN_CREATION_TIME} DESC"

        val cursor = dbHelper.readableDatabase.query(
            DATABASE_TABLE,         // The table to query
            projection,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )
        val sessions = mutableListOf<ScanSession>()
        with(cursor) {
            while (moveToNext()) {
                val session = ScanSession(
                    getString(getColumnIndexOrThrow(SessionEntry.COLUMN_TITLE)),
                    getLong(getColumnIndexOrThrow(SessionEntry.COLUMN_NUM_PHOTOS)).toInt(),
                    Date(getLong(getColumnIndexOrThrow(SessionEntry.COLUMN_CREATION_TIME))),
                    getStringOrNull(getColumnIndexOrThrow(SessionEntry.COLUMN_IMAGE)).let {
                        Uri.parse(
                            it
                        )
                    },
                    getStringOrNull(getColumnIndexOrThrow(SessionEntry.COLUMN_ZIP_FILE)).let {
                        Uri.parse(
                            it
                        )
                    })
                sessions.add(session)
            }
        }
        cursor.close()
        return sessions
    }

    class SessionDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }
    }
}