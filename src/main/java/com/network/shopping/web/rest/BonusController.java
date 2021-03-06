package com.network.shopping.web.rest;

import com.network.shopping.dto.BonusConfirmationDTO;
import com.network.shopping.dto.ShoppingDTO;
import com.network.shopping.service.BonusNetwork;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

//TODO: develop batch for creating shopping record and send request throw rest template
@RestController
@RequestMapping("/api/v1")
@Slf4j
@Api(value = "Manage shopping rewards")
public class BonusController {

    private final BonusNetwork bonusNetwork;

    @Autowired
    public BonusController(final BonusNetwork bonusNetwork) {
        this.bonusNetwork = bonusNetwork;
    }

    @PostMapping("/bonus")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = " Add reward to an account for shopping")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully added reword amount to account beneficiaries")})
    public BonusConfirmationDTO bonusComputer(@ApiParam(value = "shopping transaction data", required = true)
                                              @RequestBody @Valid final ShoppingDTO shopping) {
        log.debug("Attempt to register bonus operation for {}", shopping);
        return this.bonusNetwork.bonusAccountFor(shopping);
    }
}
