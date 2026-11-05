package com.fuad.accounting;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class AccountingDb extends SQLiteOpenHelper {
    public AccountingDb(Context c) { super(c, "fuad_accounting.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE accounts(code TEXT PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL)");
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, description TEXT)");
        db.execSQL("CREATE TABLE lines(id INTEGER PRIMARY KEY AUTOINCREMENT, transaction_id INTEGER, account_code TEXT, debit REAL, credit REAL)");
        String[][] a = {
            {"101","Kas","Aset"},{"102","Bank","Aset"},{"103","Piutang Usaha","Aset"},
            {"104","Persediaan","Aset"},{"105","Perlengkapan","Aset"},{"106","Peralatan","Aset"},
            {"201","Utang Usaha","Kewajiban"},{"202","Utang Bank","Kewajiban"},
            {"301","Modal Pemilik","Modal"},{"302","Prive","Modal"},
            {"401","Pendapatan Jasa","Pendapatan"},{"402","Penjualan","Pendapatan"},
            {"501","Beban Gaji","Beban"},{"502","Beban Sewa","Beban"},
            {"503","Beban Listrik","Beban"},{"504","Beban Perlengkapan","Beban"}
        };
        for (String[] x : a) {
            ContentValues v = new ContentValues(); v.put("code",x[0]); v.put("name",x[1]); v.put("type",x[2]); db.insert("accounts",null,v);
        }
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV) {}
    public List<String[]> accounts() {
        List<String[]> out=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT code,name,type FROM accounts ORDER BY code",null);
        while(c.moveToNext()) out.add(new String[]{c.getString(0),c.getString(1),c.getString(2)}); c.close(); return out;
    }
    public long addAccount(String code,String name,String type) {
        ContentValues v=new ContentValues();v.put("code",code);v.put("name",name);v.put("type",type);
        return getWritableDatabase().insert("accounts",null,v);
    }
    public boolean addTransaction(String date,String desc,String debitCode,String creditCode,double amount) {
        if(amount<=0 || debitCode.equals(creditCode)) return false;
        SQLiteDatabase db=getWritableDatabase(); db.beginTransaction();
        try {
            ContentValues t=new ContentValues();t.put("date",date);t.put("description",desc);
            long id=db.insertOrThrow("transactions",null,t);
            ContentValues d=new ContentValues();d.put("transaction_id",id);d.put("account_code",debitCode);d.put("debit",amount);d.put("credit",0);db.insertOrThrow("lines",null,d);
            ContentValues c=new ContentValues();c.put("transaction_id",id);c.put("account_code",creditCode);c.put("debit",0);c.put("credit",amount);db.insertOrThrow("lines",null,c);
            db.setTransactionSuccessful(); return true;
        } catch(Exception e) { return false; } finally { db.endTransaction(); }
    }
    public List<String[]> transactions() {
        List<String[]> out=new ArrayList<>();
        Cursor c=getReadableDatabase().rawQuery("SELECT t.date,t.description, SUM(l.debit),SUM(l.credit) FROM transactions t JOIN lines l ON l.transaction_id=t.id GROUP BY t.id ORDER BY t.id DESC",null);
        while(c.moveToNext()) out.add(new String[]{c.getString(0),c.getString(1),String.valueOf(c.getDouble(2)),String.valueOf(c.getDouble(3))});
        c.close();return out;
    }
    public double balance(String code) {
        Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(debit-credit),0) FROM lines WHERE account_code=?",new String[]{code});
        double x=0;if(c.moveToFirst())x=c.getDouble(0);c.close();return x;
    }
    public List<String[]> ledger(String code) {
        List<String[]> out=new ArrayList<>(); double bal=0;
        Cursor c=getReadableDatabase().rawQuery("SELECT t.date,t.description,l.debit,l.credit FROM lines l JOIN transactions t ON t.id=l.transaction_id WHERE l.account_code=? ORDER BY t.date,t.id,l.id",new String[]{code});
        while(c.moveToNext()){double d=c.getDouble(2),cr=c.getDouble(3);bal+=d-cr;out.add(new String[]{c.getString(0),c.getString(1),String.valueOf(d),String.valueOf(cr),String.valueOf(bal)});}
        c.close();return out;
    }
    public double sumType(String type) {
        double total=0; for(String[] a:accounts()) if(a[2].equals(type)){double b=balance(a[0]); total += (type.equals("Aset")||type.equals("Beban"))?b:-b;} return total;
    }
    public double cashMovement() {
        Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(debit-credit),0) FROM lines WHERE account_code IN ('101','102')",null);
        double v=0;if(c.moveToFirst())v=c.getDouble(0);c.close();return v;
    }
    public int transactionCount() {
        Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM transactions",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
    }
}
