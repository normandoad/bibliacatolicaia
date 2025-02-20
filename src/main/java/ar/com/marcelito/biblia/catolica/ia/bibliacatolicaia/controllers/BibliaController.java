package ar.com.marcelito.biblia.catolica.ia.bibliacatolicaia.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BibliaController {

	private final ChatModel chatModel;
	private final VectorStore vectorStore;

	private String prompt = """
			Tu tarea es responder en español las preguntas sobre la Sagrada Biblia Católica. Usa la información de la sección DOCUMENTOS
			para brindar respuestas precisas. Si no estás seguro o si la respuesta no se encuentra en la sección DOCUMENTOS,
			simplemente indica que no sabes la respuesta.

			QUESTION:
			{input}

			DOCUMENTS:
			{documents}

			""";

	public BibliaController(ChatModel chatModel, VectorStore vectorStore) {
		this.chatModel = chatModel;
		this.vectorStore = vectorStore;
	}

	@GetMapping("/")
	public String simplify(
			@RequestParam(defaultValue = "Dime que dice \"El Evangelio Según San Juan, Capítulo 1 de versículo 1 al 3\"") String question) {

		PromptTemplate template = new PromptTemplate(prompt);
		Map<String, Object> promptsParameters = new HashMap<>();
		promptsParameters.put("input", question);
		promptsParameters.put("documents", findSimilarData(question));

		return chatModel.call(template.create(promptsParameters)).getResult().getOutput().getText();
	}

	private String findSimilarData(String question) {
		List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(5).build());

		return documents.stream().map(document -> document.getText()).collect(Collectors.joining());

	}
}
