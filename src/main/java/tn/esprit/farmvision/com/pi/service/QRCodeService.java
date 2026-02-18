package com.pi.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.pi.model.Equipement;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class QRCodeService {

    private static final int TAILLE_QR = 300;
    private static final String DOSSIER_QR = "qr_codes";

    public Image genererQRCode(Equipement equipement) {
        String contenu = construireContenuQR(equipement);
        return genererImageQR(contenu);
    }

    public void sauvegarderQRCode(Equipement equipement) {
        String contenu = construireContenuQR(equipement);
        String nomFichier = String.format("QR_%d_%s.png",
                equipement.getId(),
                equipement.getNom().replaceAll(" ", "_"));

        try {
            // Créer le dossier si nécessaire
            java.io.File dossier = new java.io.File(DOSSIER_QR);
            if (!dossier.exists()) {
                dossier.mkdirs();
            }

            Path chemin = Paths.get(DOSSIER_QR, nomFichier);
            sauvegarderImageQR(contenu, chemin.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String construireContenuQR(Equipement equipement) {
        return String.format(
                "FARMVISION-EQUIPEMENT\n" +
                        "ID: %d\n" +
                        "Nom: %s\n" +
                        "Type: %s\n" +
                        "État: %s\n" +
                        "Date achat: %s\n" +
                        "Durée vie: %d ans\n" +
                        "URL: farmvision://equipement/%d",
                equipement.getId(),
                equipement.getNom(),
                equipement.getType(),
                equipement.getEtat(),
                equipement.getDateAchat() != null ? equipement.getDateAchat().toString() : "N/A",
                equipement.getDureeVieEstimee(),
                equipement.getId()
        );
    }

    private Image genererImageQR(String contenu) {
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrWriter.encode(
                    contenu,
                    BarcodeFormat.QR_CODE,
                    TAILLE_QR,
                    TAILLE_QR,
                    hints
            );

            // Convert BitMatrix to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(TAILLE_QR, TAILLE_QR, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < TAILLE_QR; x++) {
                for (int y = 0; y < TAILLE_QR; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // Convert BufferedImage to JavaFX Image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return new Image(bais);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sauvegarderImageQR(String contenu, String cheminFichier) throws WriterException, IOException {
        QRCodeWriter qrWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrWriter.encode(contenu, BarcodeFormat.QR_CODE, TAILLE_QR, TAILLE_QR, hints);

        Path chemin = Paths.get(cheminFichier);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", chemin);
    }

    public ImageView creerImageViewQR(Equipement equipement, int taille) {
        Image qrImage = genererQRCode(equipement);
        if (qrImage != null) {
            ImageView imageView = new ImageView(qrImage);
            imageView.setFitWidth(taille);
            imageView.setFitHeight(taille);
            imageView.setPreserveRatio(true);
            return imageView;
        }
        return null;
    }
}