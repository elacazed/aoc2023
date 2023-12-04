package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class D02 extends AoC {
    Pattern GAME_PATTERN = Pattern.compile("Game ([0-9]+): (.*)");
    Pattern DRAW_PATTERN = Pattern.compile("(([0-9]+) (red|green|blue),?)");

    public record Draw(int blue, int red, int green){

        public boolean isValid(int blue, int red, int green) {
            return this.blue <= blue && this.red <= red && this.green <= green;
        }
    };

    public class Game {
        final int id;
        final List<Draw> draws;

        public Game(String line) {
            Matcher matcher = GAME_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(line);
            }
            id = Integer.parseInt(matcher.group(1));
            String[] drawsString = matcher.group(2).split(";");
            draws = new ArrayList<>();
            for (String drawString : drawsString) {
                Matcher drawMatcher = DRAW_PATTERN.matcher(drawString);
                Map<String, Integer> colorValues = new HashMap<>();
                while (drawMatcher.find()) {
                    colorValues.put(drawMatcher.group(3), Integer.parseInt(drawMatcher.group(2)));
                }
                draws.add(new Draw(colorValues.getOrDefault("blue", 0), colorValues.getOrDefault("red", 0), colorValues.getOrDefault("green", 0)));
            }
        }

        public boolean isValid(int blue, int red, int green) {
            return draws.stream().allMatch(draw -> draw.isValid(blue, red, green));
        }
    }



    @Override
    public void run() {
        System.out.println("Possible Test Games : " + stream(getTestInputPath()).map(Game::new).filter(g -> g.isValid(14, 12, 13)).mapToInt(g -> g.id).sum());
        System.out.println("Possible Games : " + stream(getInputPath()).map(Game::new).filter(g -> g.isValid(14, 12, 13)).mapToInt(g -> g.id).sum());
    }


}
