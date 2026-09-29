import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DocumentReader {
    private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

    private static final Set<String> PLAIN = new HashSet<>(Arrays.asList(
        "txt", "md", "markdown", "csv", "tsv", "json", "xml", "html", "htm",
        "yml", "yaml", "log", "ini", "sql", "css", "js", "java", "py", "c", "cpp"));

    private static final Set<String> MARKUP = new HashSet<>(Arrays.asList("html", "htm", "xml"));

    public static String extension(File f) {
        String n = f.getName().toLowerCase();
        int dot = n.lastIndexOf('.');
        return dot < 0 ? "" : n.substring(dot + 1);
    }

    public static boolean isSupported(File f) {
        if (!f.isFile()) return false;
        String name = f.getName();
        if (name.startsWith(".") || name.startsWith("~$")) return false;   // hidden / Word temp files
        String ext = extension(f);
        return PLAIN.contains(ext) || ext.equals("docx") || ext.equals("pdf");
    }

    public static String read(File f) throws IOException {
        String ext = extension(f);
        if (ext.equals("docx")) return readDocx(f);
        if (ext.equals("pdf")) return readPdf(f);

        // decoding this way never throws on odd characters (unlike Files.readString)
        String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        if (MARKUP.contains(ext)) {
            text = text.replaceAll("(?s)<[^>]*>", " ");
        }
        return text;
    }

    private static String readDocx(File f) throws IOException {
        try (ZipFile zip = new ZipFile(f)) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            if (entry == null) throw new IOException("Not a valid .docx file");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            try (InputStream in = zip.getInputStream(entry)) {
                Document doc = dbf.newDocumentBuilder().parse(in);
                StringBuilder sb = new StringBuilder();
                NodeList paragraphs = doc.getElementsByTagNameNS(W_NS, "p");
                for (int i = 0; i < paragraphs.getLength(); i++) {
                    NodeList texts = ((Element) paragraphs.item(i)).getElementsByTagNameNS(W_NS, "t");
                    for (int j = 0; j < texts.getLength(); j++) {
                        sb.append(texts.item(j).getTextContent());
                    }
                    sb.append('\n');
                }
                return sb.toString();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Could not read docx: " + e.getMessage(), e);
        }
    }

    // Uses reflection so the project still compiles when the PDFBox jar is absent
    private static String readPdf(File f) throws IOException {
        try {
            Class<?> loader = Class.forName("org.apache.pdfbox.Loader");
            Object pdDoc = loader.getMethod("loadPDF", File.class).invoke(null, f);
            try {
                Class<?> stripperClass = Class.forName("org.apache.pdfbox.text.PDFTextStripper");
                Object stripper = stripperClass.getDeclaredConstructor().newInstance();
                Method getText = stripperClass.getMethod("getText",
                        Class.forName("org.apache.pdfbox.pdmodel.PDDocument"));
                return (String) getText.invoke(stripper, pdDoc);
            } finally {
                ((Closeable) pdDoc).close();
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("PDF support needs the PDFBox jar on the classpath");
        } catch (Exception e) {
            throw new IOException("Could not read PDF: " + e.getMessage(), e);
        }
    }
}