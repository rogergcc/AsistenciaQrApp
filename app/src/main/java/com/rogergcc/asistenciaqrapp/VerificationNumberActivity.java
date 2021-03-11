package com.rogergcc.asistenciaqrapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScanner;
import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScannerBuilder;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VerificationNumberActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = VerificationNumberActivity.class.getSimpleName();
    private static final int REQUEST_CODE_NUMBER = 1008;
    GoogleApiClient mGoogleApiClient;
    private PrefsHelper prefs;
    private TextView tvPhoneResult;
    private FloatingActionButton fabScanear;
    private final int MY_PERMISSION_REQUEST_CAMERA = 1001;
    public static final String BARCODE_KEY = "BARCODE";
    private Barcode barcodeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_number);
        tvPhoneResult = findViewById(R.id.tvPhoneResult);
        fabScanear = findViewById(R.id.fabScanear);


        String numeroCelularGuardado = getPrefs().getPhoneNumber(null);
        if (numeroCelularGuardado!=null){
            tvPhoneResult.setText(numeroCelularGuardado);

        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.CREDENTIALS_API)
                .build();


        mGoogleApiClient.connect();

        if(savedInstanceState != null){
            Barcode restoredBarcode = savedInstanceState.getParcelable(BARCODE_KEY);
            if(restoredBarcode != null){
                tvPhoneResult.setText(restoredBarcode.rawValue);
                barcodeResult = restoredBarcode;
            }
        }

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, getResources().getString(R.string.camera_permission_granted));
            startScanningBarcode();
        } else {
            requestCameraPermission();

        }
    }


    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

            ActivityCompat.requestPermissions(VerificationNumberActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CAMERA);

        } else {
            ActivityCompat.requestPermissions(VerificationNumberActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CAMERA);
        }
    }


    private String obtenerFechaActualAMPM() {

        DateFormat df = new SimpleDateFormat("KK:mm:ss a, dd/MM/yyyy", Locale.getDefault());
        return df.format(new Date());
    }

    private String obtenerFechaActual24HRS() {

        DateFormat df = new SimpleDateFormat("HH:mm:ss a dd/MM/yyyy", Locale.getDefault());
        return df.format(new Date());
    }

    private void showHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), REQUEST_CODE_NUMBER, null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            Log.e("", "Could not start hint picker Intent", e);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BARCODE_KEY, barcodeResult);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_NUMBER:
                if (resultCode == RESULT_OK) {
                    Credential cred = null;
                    if (data == null) return;

                    cred = data.getParcelableExtra(Credential.EXTRA_KEY);
//                    cred.getId====: ====+919*******
                    if (cred == null) return;

                    Log.e("cred.getId", cred.getId());
//                    userMob = cred.getId();

                    getPrefs().setPhoneNumber(cred.getId());

                    Toast.makeText(this, "Cred: " + cred.getId(), Toast.LENGTH_SHORT).show();
                    tvPhoneResult.setText(cred.getId());

                } else {
                    // Sim Card not found!
                    Log.e("cred.getId", "RC_HINT else");

                    return;
                }


                break;
        }
    }

    protected PrefsHelper getPrefs() {
        if (prefs == null) {
            prefs = new PrefsHelper(this);
        }
        return prefs;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "GoogleApiClient is suspended with cause code: " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApiClient failed to connect: " + connectionResult);
    }

    public void verificarEIngresar(View view) {
        String defaultPhone = getPrefs().getPhoneNumber(null);
        if (defaultPhone == null) {
            showHint();
        } else {

            tvPhoneResult.setText(defaultPhone);

        }


    }

    public void actionQuitarNumero(View view) {
        String nrCelularGuardado = getPrefs().getPhoneNumber(null);
        if (nrCelularGuardado != null) {
            getPrefs().removePhoneNumber();
            tvPhoneResult.setText("NR CELULAR: -");
        }

    }

    private void startScanningBarcode() {
        final String defaultPhone = getPrefs().getPhoneNumber(null);
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(VerificationNumberActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(true)
                .withBackfacingCamera()
                .withCenterTracker()
                .withOnlyQRCodeScanning()
                .withText("Scaneando...")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                         barcodeResult = barcode;

//                        dialogo guardar lista y db local
//                        showDialog(barcode.rawValue , getScanTime(),getScanDate());
                        String resultado = barcode.rawValue + "\n" + defaultPhone + "\n" + obtenerFechaActual24HRS();
                        tvPhoneResult.setText(resultado);


//                        Intent intent = new Intent(VerificationNumberActivity.this, TicketResultActivity.class);
//                        intent.putExtra("code", barcode.rawValue);
//                        startActivity(intent);
                    }
                })
                .build();

        materialBarcodeScanner.startScan();
    }

    public void clickFabScanear(View view) {
        String defaultPhone = getPrefs().getPhoneNumber(null);
        if (defaultPhone == null) {
            Toast.makeText(this, "Verificar Nr Celular", Toast.LENGTH_SHORT).show();
            return;
        }

        checkPermission();

        //startScanningBarcode();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CAMERA && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanningBarcode();
        } else {
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.sorry_for_not_permission), Snackbar.LENGTH_SHORT)
                    .show();
        }



    }

//    private void showDialog(final String scanContent, final String currentTime, final String currentDate) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(VerificationNumberActivity.this);
//
//        builder.setMessage(scanContent)
//                .setTitle(R.string.dialog_title);
//        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                DatabaseHelper databaseHelper = new DatabaseHelper(context);
//                databaseHelper.addProduct(new Product(scanContent,currentTime,currentDate));
//                Toast.makeText(VerificationNumberActivity.this, "Saved", Toast.LENGTH_SHORT).show();
//                viewPager.setCurrentItem(1);
//
//
//            }
//        });
//        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                Toast.makeText(VerificationNumberActivity.this, "Not Saved", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.show();
//    }
}