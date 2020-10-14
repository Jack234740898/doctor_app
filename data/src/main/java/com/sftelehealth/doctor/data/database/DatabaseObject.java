package com.sftelehealth.doctor.data.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.commonsware.cwac.saferoom.SafeHelperFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Created by Rahul on 22/12/17.
 */

public class DatabaseObject {

    private static DoctorDatabase dbInstance;

    public static DoctorDatabase getInstance(Context context) {
        //char[] passPhrase = "^%vfdnb318(*^afh109".toCharArray();
        // EditText passphraseField;
        //SafeHelperFactory factory = new SafeHelperFactory(passPhrase);  // SafeHelperFactory.fromUser(passPhrase.toString());

        String passPhrase = "^%vfdnb318(*^afh109";
        SafeHelperFactory factory = SafeHelperFactory.fromUser(new SpannableStringBuilder(passPhrase),SafeHelperFactory.POST_KEY_SQL_MIGRATE);
        if(dbInstance == null)
            dbInstance = Room.databaseBuilder(context,
                    DoctorDatabase.class, "doctor_db")
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2,  MIGRATION_2_3, MIGRATION_3_4)
                    .build();

        return dbInstance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // migrate from the old structure of a single category field to a embedded category aobject in the database.
            // remove the fields that are not required
            // add the data for category object
            try {
                if(database.getVersion() == 1) {
                    database.execSQL("ALTER TABLE Doctor" + " ADD COLUMN doctorCategories TEXT ");
                }

                /*Cursor doesColumnExists = database.query("SELECT doctorCategories FROM Doctor LIMIT 1");

                if(doesColumnExists != null && doesColumnExists.getCount() > 0) {
                    // Do not insert as column exists
                } else
                    database.execSQL("ALTER TABLE Doctor" + " ADD COLUMN doctorCategories TEXT " );

                doesColumnExists.close();*/
            } catch (SQLiteException ex) {
                Log.w("SQLite - Database", "Altering Doctor: " + ex.getMessage());
            }
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS SystemInfo (phone TEXT PRIMARY KEY NOT NULL, shouldUpdate INTEGER NOT NULL, mustUpdate INTEGER NOT NULL, freeFollowUpTime INTEGER NOT NULL, appointmentDaysLimit INTEGER NOT NULL, videoAcceptWaitTime INTEGER NOT NULL, dialerButtonVisibleTime INTEGER NOT NULL, countryISDCode TEXT, countryCode TEXT)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // database.execSQL("CREATE TABLE IF NOT EXISTS SystemInfo (phone TEXT PRIMARY KEY NOT NULL, shouldUpdate INTEGER NOT NULL, mustUpdate INTEGER NOT NULL, freeFollowUpTime INTEGER NOT NULL, appointmentDaysLimit INTEGER NOT NULL, videoAcceptWaitTime INTEGER NOT NULL, dialerButtonVisibleTime INTEGER NOT NULL, countryISDCode TEXT, countryCode TEXT)");
            database.execSQL("ALTER TABLE SystemInfo ADD COLUMN cliNumbers TEXT ");
            database.execSQL("ALTER TABLE SystemInfo ADD COLUMN whatsAppPhone TEXT ");
            database.execSQL("ALTER TABLE SystemInfo ADD COLUMN timezone TEXT ");
        }
    };

    private void dropColumn(SQLiteDatabase db,
                            String createTableCmd,
                            String tableName,
                            String[] colsToRemove) throws java.sql.SQLException {

        List<String> updatedTableColumns = getTableColumns(db, tableName);
        // Remove the columns we don't want anymore from the table's list of columns
        updatedTableColumns.removeAll(Arrays.asList(colsToRemove));

        String columnsSeparated = TextUtils.join(",", updatedTableColumns);

        db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;");

        // Creating the table on its new format (no redundant columns)
        db.execSQL(createTableCmd);

        // Populating the table with the data
        db.execSQL("INSERT INTO " + tableName + "(" + columnsSeparated + ") SELECT "
                + columnsSeparated + " FROM " + tableName + "_old;");
        db.execSQL("DROP TABLE " + tableName + "_old;");
    }

    public List<String> getTableColumns(SQLiteDatabase db, String tableName) {
        ArrayList<String> columns = new ArrayList<String>();
        String cmd = "pragma table_info(" + tableName + ");";
        Cursor cur = db.rawQuery(cmd, null);

        while (cur.moveToNext()) {
            columns.add(cur.getString(cur.getColumnIndex("name")));
        }
        cur.close();

        return columns;
    }

    public static void clearAllTables(){
        if (dbInstance != null) {
            dbInstance.clearAllTables();
        }
    }
}