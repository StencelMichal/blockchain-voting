// SPDX-License-Identifier: MIT
pragma solidity 0.8.17;

import { BigNumbers, BigNumber } from "./BigNumbers.sol";
//import "BigNumbers.sol";
import "Entities.sol";


contract VotingState {

    using BigNumbers for *;

    Vote public vote;
    BigNumber public hash;
    string[] public logger;
//    Vote[] public votes;

    struct RingSignature {
//        keys: List<PublicKey>,
        string startValue;
        string[] ringValues;
        string tag;
    }

    struct Vote {
        string candidate;
        RingSignature ringSignature;
    }

    function validateRsaRingSignature(RingSignature memory ringSignature, string memory signedMessage) private {
        BigNumber memory hash = cryptoHash(signedMessage);
    }

    function cryptoHash(string memory message) private returns (BigNumber memory) {
        logger.push("bytes");
        bytes memory hashBytes = abi.encodePacked(sha256(bytes(message)));
        logger.push("big numbers conversion");
        return BigNumbers.init(hashBytes, false);
    }

//    function addVote(Vote memory vote) public {
//        votes.push(vote);
//    }
//
//    function getVotes() public view returns (Vote[] memory) {
//        return votes;
//    }

    function testBigNumbers() public {
        logger.push("big numbers init");
        BigNumber memory bigNumber = BigNumbers.one();
//        string memory test = BigNumbers.test();
//        BigNumber memory bigNumber = BigNumbers.one();
//        BigNumber memory bigNumber = BigNumbers.init(256, false);
        logger.push("big numbers verify");
//        logger.push(test);

//        BigNumbers.verify(bigNumber);
    }

    function setVote(Vote memory newVote) public {
        validateRsaRingSignature(newVote.ringSignature, newVote.candidate);
        vote = newVote;
    }

    function getVote() public view returns (Vote memory) {
        return vote;
    }

    function getLogs() public view returns (string[] memory){
        return logger;
    }

    function clearLogs() public {
        logger = new string[](0);
    }

}