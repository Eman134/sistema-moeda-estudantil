package br.pucminas.karv_coins.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {
    private static final int TAMANHO_QR_CODE = 320;

    public byte[] gerarPng(String conteudo) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(
                    conteudo,
                    BarcodeFormat.QR_CODE,
                    TAMANHO_QR_CODE,
                    TAMANHO_QR_CODE,
                    Map.of(EncodeHintType.MARGIN, 1)
            );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Não foi possível gerar o QRCode do resgate.", e);
        }
    }
}
