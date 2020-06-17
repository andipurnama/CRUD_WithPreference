package com.example.crudapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.crudapp.database.Berita;
import com.example.crudapp.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

@SuppressLint("Registered")
public class MainActivity extends AppCompatActivity {
    private BeritaAdapter mAdapter;
    private List<Berita> listBerita = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noDataView;
    private DatabaseHelper db;

    TextView nama;
    Button btnLogout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        noDataView = findViewById(R.id.empty_view);

        nama = findViewById(R.id.tv_nama);
        btnLogout = findViewById(R.id.buttonLogout);

        db = new DatabaseHelper(this);

        listBerita.addAll(db.getAllBerita());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFormDialog( false , null , - 1 );
            }
        });

        mAdapter = new BeritaAdapter(this, listBerita);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyData();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView,
                new RecyclerTouchListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {

                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        showActionsDialog(position);
                    }
                }));

        // nitip nih dari project sebelah
        nama = findViewById(R.id.tv_nama);
        btnLogout = findViewById(R.id.buttonLogout);

        final SessionManager sm = new SessionManager(getApplicationContext());
        nama.setText("Selamat Datang, "+sm.getNama());
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sm.logoutUser();
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private void toggleEmptyData() {
        // you can check listBerita.size() > 0
        if (db.getBeritaCount() > 0 ) {
            noDataView.setVisibility(View.GONE);
        } else {
            noDataView.setVisibility(View.VISIBLE);
        }
    }

    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showFormDialog(true, listBerita.get(position), position);
                } else {
                    deleteBerita(position);
                }
            }
        });
        builder.show();
    }

    private void showFormDialog(final boolean isUpdate, final Berita berita, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.form_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputJudul = view.findViewById(R.id.judul);
        final EditText inputIsi = view.findViewById(R.id.isi);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!isUpdate ?
                getString(R.string.lbl_Tambah_berita) : getString(R.string.lbl_Edit_berita));

        if (isUpdate && berita != null) {
            inputJudul.setText(berita.getJudul());
            inputIsi.setText(berita.getIsi());
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(isUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        dialogBox.cancel();
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputJudul.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Isi judul Berita!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                //check if user updating toast
                if (isUpdate && berita != null) {
                    // update note berdasarkan id
                    updateBerita(inputJudul.getText().toString(),inputIsi.getText().toString(), position);
                } else {
                    // membuat notes baru
                    createBerita(inputJudul.getText().toString(),inputIsi.getText().toString());
                }
            }
        });
    }

    private void createBerita(String judul, String isi) {
        // inserting berita in db and getting
        // newly inserted berita id
        long id = db.insertBerita(judul, isi);

        // mendapatkan data terbaru dari database
        Berita berita = db.getBerita(id);

        if (berita != null) {
            // adding new note to array list at 0 position
            listBerita .add( 0 , berita);
            // refreshing the list
            mAdapter .notifyDataSetChanged();
            toggleEmptyData();
        }
    }

    private void updateBerita(String judul, String isi, int position) {
        Berita berita = listBerita.get(position);
        // updating judul
        berita.setJudul(judul);
        // updating isi
        berita.setIsi(isi);
        // updating berita in db
        db .updateBerita(berita);
        // refreshing the list
        listBerita .set(position, berita);
        mAdapter .notifyItemChanged(position);
        toggleEmptyData();
    }

    private void deleteBerita(int position) {
        db.deleteBerita(listBerita.get(position));

        // removing the berita from the list
        listBerita .remove(position);
        mAdapter .notifyItemRemoved(position);
        toggleEmptyData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // nitip nih dari project sebelah

}
// huaaaaaa pen main