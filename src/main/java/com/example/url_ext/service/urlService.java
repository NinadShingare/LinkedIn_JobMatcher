package com.example.url_ext.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class urlService {
    public static String extractTextFromPdf(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    public void getUrl() throws InterruptedException{

        System.setProperty("webdriver.chrome.driver","D:/chromedriver-win64/chromedriver.exe");

        WebDriver driver = new ChromeDriver();

        // Open LinkedIn login
        driver.get("https://www.linkedin.com/login");


        String resumeText = extractTextFromPdf("D:/Study/resume/Ninad_Shingare_linked.pdf").toLowerCase();

        // Wait for you to log in manually
        System.out.println("Please log in manually...");
        Thread.sleep(25000); // 20 seconds pause

        // Navigate to jobs page
        //driver.get("https://www.linkedin.com/jobs/search-results/?f_TPR=r3600&keywords=java%20developer&origin=JOBS_HOME_SEARCH_BUTTON");
        driver.get("https://www.linkedin.com/jobs/search-results/?f_TPR=r3600&keywords=developer");
        // Wait for page to load
        System.out.println("Navigated to jobs page.");
        Thread.sleep(5000);

        // Extract job links
        List<WebElement> links = driver.findElements(By.tagName("a"));
        Set<String> jobLinks = new HashSet<>();
        System.out.println("Found " + links.size() + " <a> tags.");
        String[] keywords = {"java", "spring","associate","software", "devops","cloud", "python","sql","database","microservices", "backend", "web", "fullstack","full stack", "application","l1","l2","l3","l4","javascript","django","node.js"};

        for (WebElement link : links) {
            String text = link.getText().toLowerCase();
            for (String kw : keywords) {
                if (text.contains(kw)) {
                    jobLinks.add(link.getAttribute("href"));
                    break;
                }
            }
        }

        System.out.println("✅ Found " + jobLinks.size() + " matching job links.");

        // Step 3: Visit each job page and extract + match
        for (String url : jobLinks) {
            try {
                driver.get(url);
                Thread.sleep(5000); // wait for job details

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement jobDesc = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div#job-details")
                ));
                String jobText = jobDesc.getText().toLowerCase();

                /*WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        (By.cssSelector("div[class*='KkHoHTxECOJlfvOwixSkytXNUGZFLzsHXgOwcg']"))
                ));
                String jobCompany=el.getText();
                System.out.println("✅ Found " + jobCompany + " matching job Company.");
                */
                int matchScore = getMatchScore(resumeText, jobText, keywords);
                int requiredYears = extractRequiredExperience(jobText);

                String[] keys = {"java", "python", "javascript", "typescript", "c++", "c#", "go", "ruby", "kotlin", "scala", "php", "rust", "swift",".net","asp.net",",net core",
                        "html", "css", "react", "angular", "vue.js", "svelte", "next.js", "node.js", "express.js", "spring boot", "django", "flask", "laravel", "ruby on rails","html5","css3",
                        "mysql", "postgresql", "mongodb", "oracle", "sql server", "sqlite", "redis", "cassandra", "elasticsearch", "hbase", "nosql", "graphql", "rest api", "json", "xml",
                        "docker", "kubernetes", "jenkins", "gitlab ci", "circleci", "github actions", "ansible", "terraform", "helm", "prometheus", "grafana", "splunk", "nginx", "apache","devops",
                        "aws", "azure", "gcp", "firebase", "heroku", "digitalocean", "cloud functions", "s3", "ec2", "lambda",
                        "junit", "testng", "selenium", "postman", "cypress", "mocha", "chai", "jest", "junit5", "playwright", "mockito",
                        "git", "github", "gitlab", "bitbucket", "svn", "jira", "confluence",
                        "microservices", "monolith", "soa", "rest", "graphql", "mvc", "mvvm", "event-driven", "reactive", "api gateway", "message queue",
                        "kafka", "rabbitmq", "activemq", "mqtt", "nats", "zeromq",
                        "maven", "gradle", "npm", "yarn", "webpack", "babel", "vite", "sbt", "ant","adobe","aem","crm",
                        "oauth2", "jwt", "cors", "authentication", "authorization", "api design", "unit testing", "integration testing", "agile", "scrum", "tdd", "bdd", "ci/cd", "scalability", "performance tuning", "load balancing", "fault tolerance"};
                List<String> missingKeywords = getMissingKeywords(keys, jobText, resumeText);
                System.out.println("🔗 " + url);
                System.out.println("Job Description Match Score: " + matchScore + "%");
                if (requiredYears != -1) {
                    System.out.println("📌 Required Experience: " + requiredYears + "+ years");
                } else {
                    System.out.println("📌 Required Experience: Not specified");
                }

                if (!missingKeywords.isEmpty()) {
                    System.out.println("❌ Missing in Resume: " + String.join(", ", missingKeywords));
                } else {
                    System.out.println("✅ All technical keywords matched!");
                }

                System.out.println("------------------------------------");

            } catch (Exception e) {
                System.out.println("❌ Failed to parse: " + url);
            }
        }

        driver.quit();
    }
    public static int getMatchScore(String resume, String jd, String[] keys) {
        int match = 0;
        for (String keyword : keys) {
            if (resume.contains(keyword.toLowerCase()) && jd.contains(keyword.toLowerCase())) {
                match++;
            }
        }
        return (int) ((match / (double) keys.length) * 100); // score in %
    }

    public static int extractRequiredExperience(String jobDescription) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*\\+?\\s*(?:years|yrs)\\s+of\\s+experience", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(jobDescription);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        // You can add more patterns here for other formats like "at least 3 years", etc.
        Pattern altPattern = Pattern.compile("(minimum|at least)\\s+(\\d+)\\s*(?:years|yrs)", Pattern.CASE_INSENSITIVE);
        Matcher altMatcher = altPattern.matcher(jobDescription);

        if (altMatcher.find()) {
            return Integer.parseInt(altMatcher.group(2));
        }

        return -1; // not found
    }

    public static List<String> getMissingKeywords(String[] keys, String jobDesc, String resumeText) {
        List<String> missing = new ArrayList<>();

        for (String keyword : keys) {
            String lowerKeyword = keyword.toLowerCase();
            if (jobDesc.contains(lowerKeyword) && !resumeText.contains(lowerKeyword)) {
                missing.add(keyword); // original keyword case
            }
        }

        return missing;
    }

}
