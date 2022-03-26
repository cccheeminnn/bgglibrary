package project.boardgamelibrary.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import project.boardgamelibrary.Model.BoardGame;
import project.boardgamelibrary.Repository.BoardGameRepository;
import project.boardgamelibrary.Service.BoardGameService;

@Controller
@RequestMapping(path="")
public class BoardGameController 
{
    private static final Logger logger = LoggerFactory.getLogger(BoardGameController.class);
    
    @Autowired
    private BoardGameService brdGameSvc;

    @Autowired
    private BoardGameRepository brdGameRepo;

    @GetMapping(path="")
    public String getHomepage() 
    {   
        //save to redis
        brdGameRepo.findAndSaveGames();
        return "homepage";
    }

    @GetMapping(path="/library")
    public String getBoardGameList(@RequestParam String page, Model m) 
    {   
        Integer pageInt = Integer.parseInt(page);
        
        //retrieve board game list
        List<BoardGame> brdGameList = brdGameRepo.retrieveBoardGame(pageInt);

        m.addAttribute("brdGameList", brdGameList);
        m.addAttribute("currPage", pageInt);
        return "library";
    }
    
}
