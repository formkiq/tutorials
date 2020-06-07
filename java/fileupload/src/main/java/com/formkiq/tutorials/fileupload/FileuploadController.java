/**
 * Copyright [2020] FormKiQ Inc. Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License
 * at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.formkiq.tutorials.fileupload;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.formkiq.stacks.client.FormKiqClient;
import com.formkiq.stacks.client.models.DocumentUrl;
import com.formkiq.stacks.client.models.Documents;
import com.formkiq.stacks.client.requests.GetDocumentContentUrlRequest;
import com.formkiq.stacks.client.requests.GetDocumentUploadRequest;
import com.formkiq.stacks.client.requests.GetDocumentsRequest;
import software.amazon.awssdk.utils.IoUtils;

@Controller
public class FileuploadController {

  /** Signup for your Cognito Sandbox Account at: https://www.formkiq.com/products/document-stack/demo */
  /** Cognito User. */
  private static final String COGNITO_USER = "REPLACE_WITH_SANDBOX_EMAIL";
  /** Cognito Password. */
  private static final String COGNITO_PASSWORD = "REPLACE_WITH_SANDBOX_PASSWORD";
  /** {@link FormKiqClient}. */
  private FormKiqClient client;

  public FileuploadController() {
    this.client = FormKiqClientBuilder.build(COGNITO_USER, COGNITO_PASSWORD);
  }

  @GetMapping("/")
  public String listUploadedFiles(Model model) throws IOException, InterruptedException {

    /* The Date / TZ are not required, but defaults to current UTC date without them. */
    String tz = new SimpleDateFormat("Z").format(new Date());
    GetDocumentsRequest req = new GetDocumentsRequest().date(new Date()).tz(tz);

    Documents results = this.client.getDocuments(req);

    model.addAttribute("files",
        results.documents().stream()
            .map(doc -> Map.of("name", doc.path(), "path",
                MvcUriComponentsBuilder
                    .fromMethodName(FileuploadController.class, "serveFile", doc.documentId())
                    .build().toUri().toString()))
            .collect(Collectors.toList()));

    return "uploadForm";
  }

  @GetMapping("/files/{documentId:.+}")
  @ResponseBody
  public ResponseEntity<Resource> serveFile(@PathVariable String documentId)
      throws IOException, InterruptedException {

    GetDocumentContentUrlRequest req = new GetDocumentContentUrlRequest().documentId(documentId);
    DocumentUrl content = this.client.getDocumentContentUrl(req);

    /* Redirect Request to the Content URL (which is an S3 Presigned Url). */
    return ResponseEntity.status(307).header("Location", content.url()).build();
  }

  @PostMapping("/")
  public String handleFileUpload(@RequestParam("file") final MultipartFile file,
      final RedirectAttributes redirectAttributes) throws IOException, InterruptedException {

    long contentLength = file.getSize();

    /* FormKiQ Demo restricts the ContentLength, so contentLength MUST be specified. */
    GetDocumentUploadRequest uploadRequest = new GetDocumentUploadRequest()
        .path(file.getOriginalFilename()).contentLength(contentLength);

    // Retrieve URL that can be used to upload document.
    DocumentUrl upload = this.client.getDocumentUpload(uploadRequest);

    // Upload Document
    InputStream inputStream = file.getInputStream();

    BodyPublisher ofContent = BodyPublishers.ofByteArray(IoUtils.toByteArray(inputStream));

    /*
     * Because ContentLength is specified above, the ByteArray must be used instead of InputStream.
     * This is because using an InputStream does not set the 'Content-Length' header. If
     * Content-Length isn't specified you can use InputStream directly. As shown below.
     */
    // BodyPublisher ofContent = BodyPublishers.ofInputStream(() -> inputStream);

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(upload.url())).method("PUT", ofContent).build();

    HttpResponse<String> response =
        HttpClient.newHttpClient().send(request, BodyHandlers.ofString());

    if (response.statusCode() != FormKiqClient.HTTP_STATUS_OK) {
      throw new IOException(response.body());
    }

    redirectAttributes.addFlashAttribute("message",
        "You successfully uploaded " + file.getOriginalFilename() + "!");

    return "redirect:/";
  }
}
