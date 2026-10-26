import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DocumentReader {
    private static final String WORD_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

    private static final Set<String> PLAIN = Set.of(
        "txt", "md", "markdown", "csv", "tsv", "json", "xml", "html", "htm",
        "yml", "yaml", "log", "ini", "sql", "css", "js", "java", "py", "c", "cpp");

    private static final Set<String> MARKUP = Set.of("html", "htm", "xml");

    private static final Pattern SCRIPT_OR_STYLE = Pattern.compile("(?is)<(script|style)\\b[^>]*>.*?</\\1>");
    private static final Pattern TAG = Pattern.compile("(?s)<[^>]*>");

    private DocumentReader() {
    }

    public static boolean isSupported(File f) {
        if (!f.isFile()) return false;
        String name = f.getName();
        if (name.startsWith(".") || name.startsWith("~$")) return false;   

        String ext = extension(f);
        return PLAIN.contains(ext) || ext.equals("docx") || ext.equals("pdf");
    }

    private static String extension(File f) {
        String name = f.getName().toLowerCase();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }

    public static String read(File f) throws IOException {
        String ext = extension(f);
        if (ext.equals("docx")) return readDocx(f);
        if (ext.equals("pdf")) return readPdf(f);

        String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        if (MARKUP.contains(ext)) {
            text = TAG.matcher(SCRIPT_OR_STYLE.matcher(text).replaceAll(" ")).replaceAll(" ");
        }
        return text;
    }

    private static String readDocx(File f) throws IOException {
        try (ZipFile zip = new ZipFile(f)) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            if (entry == null) throw new IOException("Not a valid .docx file");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            try (InputStream in = zip.getInputStream(entry)) {
                Document doc = factory.newDocumentBuilder().parse(in);
                StringBuilder text = new StringBuilder();
                NodeList paragraphs = doc.getElementsByTagNameNS(WORD_NS, "p");
                for (int i = 0; i < paragraphs.getLength(); i++) {
                    NodeList runs = ((Element) paragraphs.item(i)).getElementsByTagNameNS(WORD_NS, "t");
                    for (int j = 0; j < runs.getLength(); j++) {
                        text.append(runs.item(j).getTextContent());
                    }
                    text.append('\n');
                }
                return text.toString();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Could not read .docx: " + e.getMessage(), e);
        }
    }

    
    private static String readPdf(File f) throws IOException {
        try {
            Class<?> loader = Class.forName("org.apache.pdfbox.Loader");
            Object pdf = loader.getMethod("loadPDF", File.class).invoke(null, f);

            try {
                Class<?> pdfDocument = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
                Class<?> stripperClass = Class.forName("org.apache.pdfbox.text.PDFTextStripper");
                Object stripper = stripperClass.getDeclaredConstructor().newInstance();
                Method getText = stripperClass.getMethod("getText", pdfDocument);
                return (String) getText.invoke(stripper, pdf);
            } finally {
                ((Closeable) pdf).close();
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("PDF support needs PDFBox 3.x on the classpath");
        } catch (Exception e) {
            throw new IOException("Could not read PDF: " + e.getMessage(), e);
        }
    }
}
