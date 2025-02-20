package ar.com.marcelito.biblia.catolica.ia.bibliacatolicaia.dataloaders;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
@Log4j2
@Component
public class DataLoader {

	private final VectorStore vectorStore;

	private final JdbcClient jdbcClient;

	@Value("classpath:/La-Santa-Biblia-version-mons-Juan-Straubinger-ESPANOL-1956-ocr.pdf")
	private Resource pdfResource;

	public DataLoader(VectorStore vectorStore, JdbcClient jdbcClient) {
		this.vectorStore = vectorStore;
		this.jdbcClient = jdbcClient;
	}

	@PostConstruct
	public void init() {
		Integer count = jdbcClient.sql("select COUNT(*) from vector_store").query(Integer.class).single();

		log.info("No of Records in the PG Vector Store = " + count);

		if (count == 0) {
			log.info("Loading Catholic's Holy Bible");
			PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder().withPagesPerDocument(1).build();

			PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, config);

			var textSplitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
			List <Document> td=textSplitter.apply(reader.get());
			vectorStore.accept(td);

			log.info("Application is ready to Serve the Requests");
		}
	}
}
