package cn.huicheng.hc_101.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 *
 * SQL数据库，保存IR设备名，蓝牙地址，设备地址
 *
 * **/
public class Mysql extends SQLiteOpenHelper {
	private static final String sqlname = "mysql.db";
	private static final int Version = 2;

	public Mysql(Context context) {
		super(context, sqlname, null, Version);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String str_sql = "create table ir(devname varchar(10),address varchar(10),dev_address varchar(2));";
		db.execSQL(str_sql);//创建数据库操作语句

	}

	/*
	 * 
	 * 更新数据库
	 * */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
