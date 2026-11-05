package com.fuad.accounting;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    AccountingDb db; LinearLayout root, content; String page="Dashboard";
    final String[] types={"Aset","Kewajiban","Modal","Pendapatan","Beban"};
    int navy=Color.rgb(20,35,59), blue=Color.rgb(37,99,235);
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new AccountingDb(this);show("Dashboard");}
    TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(30,41,59));if(bold)t.setTypeface(null,1);t.setPadding(4,7,4,7);return t;}
    LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(14,8,14,14);return l;}
    LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);return l;}
    Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setOnClickListener(v->r.run());return b;}
    void show(String p){
        page=p; root=vertical();root.setBackgroundColor(Color.rgb(245,247,251));
        TextView title=text("FUAD ACCOUNTING",20,true);title.setTextColor(Color.WHITE);title.setBackgroundColor(navy);title.setPadding(16,18,12,18);root.addView(title);
        TextView sub=text("Aplikasi siklus akuntansi • Offline",12,false);sub.setTextColor(Color.DKGRAY);root.addView(sub);
        HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout menu=horizontal();
        String[] pages={"Dashboard","Akun","Jurnal","Buku Besar","Neraca Saldo","Laporan"};
        for(String x:pages)menu.addView(button(x,()->show(x)));hs.addView(menu);root.addView(hs);
        ScrollView scroll=new ScrollView(this);content=vertical();scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(p.equals("Dashboard")) dashboard(); else if(p.equals("Akun")) accounts(); else if(p.equals("Jurnal")) journal(); else if(p.equals("Buku Besar")) ledger(); else if(p.equals("Neraca Saldo")) trial(); else reports();
        setContentView(root);
    }
    String money(double n){return "Rp"+String.format(Locale.forLanguageTag("id-ID"),"%,.0f",n);}
    void heading(String s){content.addView(text(s,18,true));}
    void line(String s){content.addView(text(s,14,false));}
    void card(String s){TextView t=text(s,14,true);t.setBackgroundColor(Color.WHITE);t.setPadding(14,14,14,14);content.addView(t);}
    void dashboard(){
        heading("Dashboard"); double rev=db.sumType("Pendapatan"),exp=db.sumType("Beban"),profit=rev-exp;
        card("Total Aset\n"+money(db.sumType("Aset")));card("Pendapatan\n"+money(rev));card("Beban\n"+money(exp));card((profit>=0?"Laba bersih\n":"Rugi bersih\n")+money(Math.abs(profit)));
        heading("Siklus akuntansi");String[] steps={"1. Catat bukti transaksi","2. Jurnal umum","3. Posting buku besar","4. Neraca saldo","5. Laporan keuangan"};
        for(String s:steps)line("✓  "+s);
        heading("Transaksi terbaru");for(String[] t:db.transactions().subList(0,Math.min(5,db.transactions().size())))line(t[0]+" • "+t[1]+" • "+money(Double.parseDouble(t[2])));
        content.addView(button("＋ Catat transaksi",()->show("Jurnal")));
    }
    void accounts(){
        heading("Daftar Akun (COA)");for(String[] a:db.accounts())line(a[0]+"  •  "+a[1]+"  ("+a[2]+")");
        content.addView(button("＋ Tambah akun",()->accountDialog()));
    }
    void accountDialog(){
        LinearLayout box=vertical();EditText code=field("Kode akun",false),name=field("Nama akun",false);Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,types));box.addView(code);box.addView(name);box.addView(type);
        new AlertDialog.Builder(this).setTitle("Tambah akun").setView(box).setPositiveButton("Simpan",(d,w)->{if(code.getText().length()>0&&name.getText().length()>0){long n=db.addAccount(code.getText().toString(),name.getText().toString(),types[type.getSelectedItemPosition()]);if(n==-1)toast("Kode akun sudah digunakan");else show("Akun");}else toast("Lengkapi data akun");}).setNegativeButton("Batal",null).show();
    }
    EditText field(String hint,boolean numeric){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);if(numeric)e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);return e;}
    void journal(){
        heading("Jurnal Umum");content.addView(text("Masukkan satu transaksi dengan satu akun debit dan satu akun kredit. Untuk transaksi majemuk, pecah menjadi beberapa jurnal.",12,false));
        content.addView(button("＋ Input transaksi baru",()->transactionDialog()));
        heading("Riwayat jurnal");List<String[]> ts=db.transactions();if(ts.isEmpty())line("Belum ada transaksi.");for(String[] t:ts)line(t[0]+" | "+t[1]+"\nDebit "+money(Double.parseDouble(t[2]))+" • Kredit "+money(Double.parseDouble(t[3])));
    }
    void transactionDialog(){
        LinearLayout box=vertical();EditText date=field("Tanggal YYYY-MM-DD",false);date.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));
        EditText desc=field("Keterangan transaksi",false),amount=field("Nominal rupiah",true);
        List<String[]> ac=db.accounts();String[] labels=new String[ac.size()];for(int i=0;i<ac.size();i++)labels[i]=ac.get(i)[0]+" - "+ac.get(i)[1];
        Spinner debit=new Spinner(this),credit=new Spinner(this);ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels);debit.setAdapter(ad);credit.setAdapter(ad);
        box.addView(date);box.addView(desc);box.addView(text("Akun debit",13,true));box.addView(debit);box.addView(text("Akun kredit",13,true));box.addView(credit);box.addView(amount);
        new AlertDialog.Builder(this).setTitle("Transaksi baru").setView(box).setPositiveButton("Simpan",(d,w)->{
            try{double val=Double.parseDouble(amount.getText().toString());boolean ok=db.addTransaction(date.getText().toString(),desc.getText().toString(),ac.get(debit.getSelectedItemPosition())[0],ac.get(credit.getSelectedItemPosition())[0],val);if(ok){toast("Jurnal tersimpan di perangkat");show("Jurnal");}else toast("Periksa nominal dan akun");}catch(Exception e){toast("Nominal tidak valid");}
        }).setNegativeButton("Batal",null).show();
    }
    void ledger(){
        heading("Buku Besar");List<String[]> ac=db.accounts();String[] labels=new String[ac.size()];for(int i=0;i<ac.size();i++)labels[i]=ac.get(i)[0]+" - "+ac.get(i)[1];
        Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));content.addView(sp);
        LinearLayout results=vertical();content.addView(results);
        Runnable update=()->{results.removeAllViews();String code=ac.get(sp.getSelectedItemPosition())[0];for(String[] x:db.ledger(code))results.addView(text(x[0]+" | "+x[1]+"\nDebit "+money(Double.parseDouble(x[2]))+" • Kredit "+money(Double.parseDouble(x[3]))+" • Saldo "+money(Double.parseDouble(x[4])),13,false));if(results.getChildCount()==0)results.addView(text("Belum ada mutasi.",13,false));};
        sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int pos,long id){update.run();}});update.run();
    }
    void trial(){
        heading("Neraca Saldo");double d=0,c=0;
        for(String[] a:db.accounts()){double raw=db.balance(a[0]);boolean debit=a[2].equals("Aset")||a[2].equals("Beban");double dr=debit?raw:-raw;if(Math.abs(dr)>0.001){double x=Math.max(0,dr),y=Math.max(0,-dr);d+=x;c+=y;line(a[0]+" "+a[1]+"\nDebit "+money(x)+" | Kredit "+money(y));}}
        card("TOTAL DEBIT  "+money(d)+"\nTOTAL KREDIT  "+money(c)+"\n"+(Math.abs(d-c)<0.01?"SEIMBANG":"Periksa saldo"));
    }
    void reports(){
        heading("Laporan Laba Rugi");double rev=db.sumType("Pendapatan"),exp=db.sumType("Beban"),profit=rev-exp;
        card("Pendapatan: "+money(rev)+"\nBeban: "+money(exp)+"\n"+(profit>=0?"Laba bersih: ":"Rugi bersih: ")+money(Math.abs(profit)));
        heading("Laporan Posisi Keuangan");double asset=db.sumType("Aset"),liab=db.sumType("Kewajiban"),equity=db.sumType("Modal")+profit;
        card("Aset: "+money(asset)+"\nKewajiban: "+money(liab)+"\nModal + laba berjalan: "+money(equity)+"\nSelisih: "+money(asset-liab-equity));
        heading("Ringkasan kas dan bank");card("Perubahan bersih kas/bank: "+money(db.cashMovement()));
        content.addView(text("Catatan: laporan pembelajaran. Laba periode berjalan dimasukkan ke ekuitas. Arus kas belum diklasifikasikan ke aktivitas operasi, investasi, dan pendanaan.",12,false));
    }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
