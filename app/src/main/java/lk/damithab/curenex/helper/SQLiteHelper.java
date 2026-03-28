package lk.damithab.curenex.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "clinic.db";
    private static final int DB_VERSION = 1;

    private static SQLiteHelper sqLiteHelper;

    private SQLiteHelper(@Nullable Context context) {
        super(context, SQLiteHelper.DATABASE_NAME, null, SQLiteHelper.DB_VERSION);
    }

    public static synchronized SQLiteHelper getInstance(Context context) {
        if (sqLiteHelper == null) {
            sqLiteHelper = new SQLiteHelper(context.getApplicationContext());
        }
        return sqLiteHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String tableQuery = "CREATE TABLE IF NOT EXISTS `clinic` (id INTEGER PRIMARY KEY AUTOINCREMENT, name " +
                "VARCHAR(150), address VARCHAR(100), emergency VARCHAR(15), email VARCHAR(100), phone VARCHAR(15))"; //use AUTOINCREMENT instead of mysql AUTO_INCREMENT
        sqLiteDatabase.execSQL(tableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String dropTable = "DROP TABLE IF EXISTS `clinic`";
        sqLiteDatabase.execSQL(dropTable);
        onCreate(sqLiteDatabase);
    }
}


