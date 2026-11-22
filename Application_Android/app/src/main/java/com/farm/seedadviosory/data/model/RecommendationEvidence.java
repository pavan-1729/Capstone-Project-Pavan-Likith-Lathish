package com.farm.seedadviosory.data.model;

/**
 * Evidence that supports a recommendation.
 */
public class RecommendationEvidence {
    private String sourceOrganization;
    private String sourceDocument;
    private String sourceType;
    private String pdfPage;
    private String printedPage;
    private String sourceText;
    private String sourceUrl;
    private String relevance;

    public RecommendationEvidence() { }

    public RecommendationEvidence(String sourceOrganization, String sourceDocument,
                                   String sourceType, String pdfPage,
                                   String printedPage, String sourceText) {
        this.sourceOrganization = sourceOrganization;
        this.sourceDocument = sourceDocument;
        this.sourceType = sourceType;
        this.pdfPage = pdfPage;
        this.printedPage = printedPage;
        this.sourceText = sourceText;
    }

    public String getSourceOrganization() { return sourceOrganization; }
    public void setSourceOrganization(String s) { this.sourceOrganization = s; }

    public String getSourceDocument() { return sourceDocument; }
    public void setSourceDocument(String s) { this.sourceDocument = s; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String s) { this.sourceType = s; }

    public String getPdfPage() { return pdfPage; }
    public void setPdfPage(String s) { this.pdfPage = s; }

    public String getPrintedPage() { return printedPage; }
    public void setPrintedPage(String s) { this.printedPage = s; }

    public String getSourceText() { return sourceText; }
    public void setSourceText(String s) { this.sourceText = s; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String s) { this.sourceUrl = s; }

    public String getRelevance() { return relevance; }
    public void setRelevance(String s) { this.relevance = s; }
}
