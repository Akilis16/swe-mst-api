package com.project.mst.TicTacToe;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.mst.module.users.UserModel;

@RestController
@RequestMapping("/tictactoe")
class TicTacToeController
{
    @GetMapping("/getMSG")
    public String getMSG()
    {
        
        return "Jogo da velha!";
    }
}