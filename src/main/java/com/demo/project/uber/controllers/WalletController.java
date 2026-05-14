package com.demo.project.uber.controllers;

import com.demo.project.uber.dto.WalletDto;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.Wallet;
import com.demo.project.uber.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final ModelMapper modelMapper;

    @PostMapping("/withdraw")
    @Secured("ROLE_DRIVER")
    public ResponseEntity<WalletDto> withdrawAllMyMoney(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Wallet wallet = walletService.withdrawAllMyMoneyFromWallet(user);
        return ResponseEntity.ok(modelMapper.map(wallet, WalletDto.class));
    }
}
