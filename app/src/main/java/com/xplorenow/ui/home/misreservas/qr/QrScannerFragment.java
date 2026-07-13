package com.xplorenow.ui.home.misreservas.qr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.xplorenow.R;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrScannerFragment extends Fragment {

    private static final String TAG = "QrScannerFragment";
    public static final String ARG_VOUCHER_CODIGO = "voucherCodigo";

    private PreviewView previewView;
    private MaterialToolbar toolbar;
    private ExecutorService cameraExecutor;
    private boolean yaEscaneado = false;
    private String voucherEsperado;

    private final ActivityResultLauncher<String> permisoCamara =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            iniciarCamara();
                        } else {
                            Navigation.findNavController(requireView()).popBackStack();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);
        toolbar = view.findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        voucherEsperado = requireArguments().getString(ARG_VOUCHER_CODIGO, "");
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            permisoCamara.launch(Manifest.permission.CAMERA);
        }
    }

    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();
                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (yaEscaneado) {
                        imageProxy.close();
                        return;
                    }

                    @SuppressWarnings("UnsafeOptInUsageError")
                    InputImage image = InputImage.fromMediaImage(
                            imageProxy.getImage(),
                            imageProxy.getImageInfo().getRotationDegrees());

                    scanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {
                                    String valor = barcode.getRawValue();
                                    if (valor != null && !yaEscaneado) {
                                        yaEscaneado = true;
                                        procesarQr(valor);
                                    }
                                }
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error escaneando", e))
                            .addOnCompleteListener(task -> imageProxy.close());
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error iniciando camara", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void procesarQr(String valorLeido) {
        if (getView() == null) return;

        requireActivity().runOnUiThread(() -> {
            Bundle args = new Bundle();
            args.putBoolean("exitoso", valorLeido.equals(voucherEsperado));
            args.putString("mensajeQr", valorLeido);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_qrScanner_to_checkinResultado, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}