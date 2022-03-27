package project.boardgamelibrary.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import project.boardgamelibrary.Model.BoardGame;
import project.boardgamelibrary.Repository.BoardGameRepository;

@Controller
@RequestMapping(path="")
public class BoardGameController 
{
    @Autowired
    private BoardGameRepository brdGameRepo;

    //first visit, save and goes to homepage
    @GetMapping(path="")
    public String getHomepage() 
    {   
        //save to redis
        brdGameRepo.findAndSaveGames();
        return "homepage";
    }

    //homepage has a button Library, goes to fetch page 1 and display, same layout
    @GetMapping(path="/library")
    public String getBoardGameList(@RequestParam String page, Model m) 
    {   
        Integer pageInt;
        try {
            pageInt = Integer.parseInt(page);
            if (pageInt < 1) {
                pageInt = 1;
            }
        //if nonsense is typed, default page 1
        } catch (NumberFormatException e) {
            pageInt = 1;
        }
        
        //retrieve board game list
        List<BoardGame> brdGameList = brdGameRepo.retrieveBoardGame(pageInt);

        m.addAttribute("brdGameList", brdGameList);
        m.addAttribute("currPage", pageInt);
        return "library";
    }
    
    //search button is pressed
    @GetMapping(path="/library/search")
    public String getSearchResult(@RequestParam(required = false) String gameName, 
        @RequestParam(required = false) String gameId, Model m) 
    {
        List<BoardGame> brdGameList = brdGameRepo.retrieveSearches(gameName, gameId);

        m.addAttribute("brdGameList", brdGameList);
        //a different template, this returns all searched result to the user in one page
        return "searchresult";
    }
}
