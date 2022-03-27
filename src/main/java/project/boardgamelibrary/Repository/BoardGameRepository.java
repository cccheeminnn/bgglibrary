package project.boardgamelibrary.Repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import project.boardgamelibrary.Model.BoardGame;

@Repository
public class BoardGameRepository {
    private static final Logger logger = LoggerFactory.getLogger(BoardGameRepository.class);
    
    @Autowired
    @Qualifier("boardgames")
    private RedisTemplate<String, String> redisTemplate;
    
    public void findAndSaveGames() 
    {
        //if its been saved it wont save again, assuming database size fixed
        if (!redisTemplate.hasKey("boardgames")) 
        {
            //instead of ResponseEntity or cmd line args classLoader searches for the file
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("static/game.json")) {
                JsonReader reader = Json.createReader(is);
                JsonArray array = reader.readArray();
                
                array.stream().iterator().forEachRemaining(v -> {
                    JsonObject o = v.asJsonObject();
                    redisTemplate.opsForList().rightPush(
                        "boardgames", //key
                        o.toString()); //value
                });
            } catch (FileNotFoundException e) {
                logger.error(">>> FileNotFoundException for game.json");
            } catch (IOException e) {
                logger.error(">>> IOExcepion for game.json fis");
            }
        }
    }

    public List<BoardGame> retrieveBoardGame(Integer pageNumber) 
    {
        List<String> retrievedListStr = new ArrayList<>();
        //parse in the 10 elements according to the page number
        retrievedListStr = redisTemplate.opsForList().range(
            "boardgames", //key
            0+10*(pageNumber-1), //first index
            9+10*(pageNumber-1)); //last index
        
        List<BoardGame> retrievedListBrdGame = new ArrayList<>();
        //each String was converted from a JsonObject, now each String is parse
        //back as JsonObject and a new instance of BoardGame is created then saved
        //into List<BoardGame>
        retrievedListStr.stream().forEach(s -> {
            JsonReader reader = Json.createReader(new StringReader(s));
            JsonObject object = reader.readObject();
            
            BoardGame bGame = new BoardGame();
            bGame.setName(object.getString("name"));
            bGame.setGid(object.getInt("gid"));
            bGame.setUrl(object.getString("url"));
            bGame.setImage(object.getString("image"));
            retrievedListBrdGame.add(bGame);
        });

        return retrievedListBrdGame;
    }

    public List<BoardGame> retrieveSearches(String gameName, String gameId) 
    {
        logger.info(">>> Repo retrieveSearches " + gameName + " " + gameId);
        gameName = gameName.toLowerCase();

        List<BoardGame> allBoardGames = new ArrayList<>();
        //parse all board games from db to List<String>
        List<String> searchResultList = redisTemplate.opsForList().range("boardgames", 0, -1);
        //convert back to JsonObject > object of BoardGame class > put inside List<BoardGame>
        searchResultList.stream().forEach(s -> {
            JsonReader reader = Json.createReader(new StringReader(s));
            JsonObject object = reader.readObject();

            BoardGame brdGame = new BoardGame();
            brdGame.setName(object.getString("name"));
            brdGame.setGid(object.getInt("gid"));
            brdGame.setUrl(object.getString("url"));
            brdGame.setImage(object.getString("image"));
            allBoardGames.add(brdGame);
            });
        //using the allBoardGames list to do the filter and return the result to the list below
        List<BoardGame> resultList = new ArrayList<>();
        
        //scenario 1, as long as gameId is input means the person know the exact Gid
        if (!gameId.isBlank()) {
            Integer gameIdInt = Integer.parseInt(gameId);
            for (BoardGame bg : allBoardGames) {
                if (bg.getGid() == gameIdInt) {
                    resultList.add(bg);
                }
            }
            return resultList;
        //scenario 2, only gameName is input
        } else if (!gameName.isBlank()) {
            for (BoardGame bg : allBoardGames) {
                if (bg.getName().toLowerCase().contains(gameName)) {
                    resultList.add(bg);
                }
            }
            return resultList;
        }
        return resultList;
    }
}