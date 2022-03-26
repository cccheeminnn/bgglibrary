package project.boardgamelibrary.Repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
        if (!redisTemplate.hasKey("boardgames")) 
        {
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
        List<BoardGame> retrievedListBrdGame = new ArrayList<>();
        List<String> retrievedListStr = new ArrayList<>();

        retrievedListStr = redisTemplate.opsForList().range
            ("boardgames", //key
            0+10*(pageNumber-1), //first index
            9+10*(pageNumber-1)); //last index
            
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
}