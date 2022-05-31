package com.example.demo.parser;

import com.example.demo.Gpu;
import com.example.demo.persistance.Subscription;
import com.example.demo.persistance.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class AmazonGpuParser implements GpuParser {

    private final SubscriptionRepository subscribeRepository;
    private final String SEARCH_URL = "https://www.amazon.com/s?k=%s&page=%s&rh=n%%3A17923671011%%2Cn%%3A284822";
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36";

    @Autowired
    public AmazonGpuParser(SubscriptionRepository subscribeRepository) {
        this.subscribeRepository = subscribeRepository;
    }

    @Override
    public List<Gpu> parse() {
        List<Subscription> subscriptions = subscribeRepository.findAll();
        List<Gpu> gpuResultList = new ArrayList<>();

        for(Subscription subscription: subscriptions){

            String subName = subscription.getName();

            log.info("Start parsing: " + subName);

            String baseUrl = String.format(SEARCH_URL, subName, 1);
            try {
                Document baseDocument = Jsoup.connect(baseUrl).header("user-agent", USER_AGENT).get();

                Element pagination = baseDocument.getElementsByClass("a-pagination").first();

                if(pagination != null){
                    List<Integer> pages = parsePagination(pagination);
                    for (Integer page: pages){
                        Document gpuPage = Jsoup.connect(String.format(SEARCH_URL, subName, page)).header("user-agent", USER_AGENT).get();

                        List<Gpu> gpuList = parse(gpuPage, subscription);
                        gpuResultList.addAll(gpuList);
                    }
                }

            } catch (IOException e) {
                log.error("Can't get page", e);
            }
        }

        return gpuResultList;
    }

    private List<Gpu> parse(@NotNull Document page, Subscription subscription){
        Elements products = page.getElementsByAttributeValueContaining("data-component-type", "s-search-result");

        List<Gpu> gpuList = new ArrayList<>();

        for(Element product: products){
            Gpu gpu = new Gpu();
            Elements links = product.getElementsByClass("a-link-normal a-text-normal");
            Element link = links.first();

            if(link != null){
                String href = link.attr("href");
                String replacedHref = href.replaceAll("ref.+", "");
                gpu.setUrl("https://www.amazon.com" + replacedHref);
                String linkText = link.text();

                if(linkText != null){
                    if(!linkText.contains(subscription.getName())){
                        continue;
                    }
                }

                gpu.setName(linkText);
            }

            Elements prices = product.getElementsByClass("a-price");
            Element priceElement = prices.first();

            if(priceElement != null){
                Element price = priceElement.getElementsByClass("a-offscreen").first();
                if(price != null){
                    String priceText = price.text();
                    if(priceText == null || priceText.equals("null") || priceText.isEmpty()){
                        continue;
                    }
                    gpu.setPrice(priceText);
                }
            } else {
                continue;
            }

            log.info("Parsed gpu: " + gpu);
            gpuList.add(gpu);
        }
        return gpuList;
    }

    private List<Integer> parsePagination(@NotNull Element pagination){
        Elements disabled = pagination.getElementsByClass("a-disabled");

        Elements links = pagination.getElementsByTag("a");
        Integer lastIndex = links.stream().filter(l -> l.text().matches("[0-9]+")).map(e -> Integer.parseInt(e.text())).max(Integer::compare).get();

        List<Integer> pages = new ArrayList<>();

        for (int i = 2; i <= lastIndex; i++){
            pages.add(i);
        }

        return pages;
    }
}
