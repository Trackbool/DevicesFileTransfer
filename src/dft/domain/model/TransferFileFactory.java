package dft.domain.model;

import java.io.File;

public class TransferFileFactory {
    public static TransferFile getFromFile(File file) {
        return new TransferFileLocal(file);
    }
}
