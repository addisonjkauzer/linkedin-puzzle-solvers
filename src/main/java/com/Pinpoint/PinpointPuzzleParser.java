package com.Pinpoint;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PinpointPuzzleParser {

    public PinpointPuzzle parse(String html) {
        Document document = Jsoup.parse(html);
        Element board = document.selectFirst(".pinpoint__board");
        if (board == null) {
            throw new RuntimeException("Could not find pinpoint__board in HTML");
        }

        List<String> clues = new ArrayList<>();
        Elements cards = board.select(".pinpoint__card__container");
        for (Element card : cards) {
            Element clueEl = card.selectFirst(".pinpoint__card--clue span");
            if (clueEl != null) {
                clues.add(clueEl.text());
            }
        }

        return new PinpointPuzzle(clues);
    }
}
