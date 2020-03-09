package com.vinnjeru.roomdb2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.vinnjeru.roomdb2.Biom.Helper;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Authent extends AppCompatActivity {

    private KeyStore keyStore;
    private static final String KEY_NAME = "fprint";
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    private KeyGenerator keyGenerator;
    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;

    public static AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (!fingerprintManager.isHardwareDetected()) {

                Toast.makeText(this, "Your device doesn't support fingerprint authentication", Toast.LENGTH_SHORT).show();
            }
            /* Runtime permission */
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                    != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Please enable the fingerprint permission", Toast.LENGTH_SHORT).show();
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "No fingerprint found. Please register minimum one fingerprint", Toast.LENGTH_SHORT).show();
            }
            if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(this, "Please enable lockscreen password in your device's Settings", Toast.LENGTH_SHORT).show();
            } else {
                showAlertDialog();
                try {
                    generateEncryptionKey();
                } catch (Authent.FingerprintAuthException e) {
                    e.printStackTrace();
                }

                if (initializeCipher()) {
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    Helper helper = new Helper(this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
    }

    private void generateEncryptionKey() throws FingerprintAuthException {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintAuthException(exc);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean initializeCipher() {
        try {

            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private class FingerprintAuthException extends Exception {
        public FingerprintAuthException(Exception e) {
            super(e);
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Authent.this);
        alertDialog.setTitle("Jitambulishe");
        alertDialog.setMessage("Place your finger on the sensor");
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
                System.exit(0);
            }
        });
        alertDialog.setCancelable(false);
        alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
}
