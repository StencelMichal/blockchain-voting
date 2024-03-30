// SPDX-License-Identifier: MIT
pragma solidity 0.8.17;

import {BigNumbers, BigNumber} from "./BigNumbers.sol";
import "Base64.sol";

contract VotingState {

    Vote[] public votes;
    BigNumber public hash;
    string[] public logger;
    PaillierPublicKey public commonEncryptionKey;
    string[] public candidates;
    VotingState public state;
    string[] public voterQuestions;
    RsaPublicKey[] public votersPublicKeys;
//    Vote[] public votes;


    enum VotingState {INITIALIZATION, VOTING, TALLYING}

    function initializeVoting(
        PaillierPublicKey memory key,
        string[] memory _candidates,
        string[] memory _voterQuestions,
        RsaPublicKey[] memory _votersPublicKeys
    ) public {
        // TODO assert bootnode
        commonEncryptionKey = key;
        candidates = _candidates;
        voterQuestions = _voterQuestions;
//        votersPublicKeys = new RsaPublicKey[](_votersPublicKeys.length);
        for (uint i = 0; i < _votersPublicKeys.length; i++) {
            votersPublicKeys.push(_votersPublicKeys[i]);
        }
        state = VotingState.VOTING;
    }

    function getCandidates() public view returns (string[] memory) {
        return candidates;
    }

    function getVoterQuestions() public view returns (string[] memory) {
        return voterQuestions;
    }

    function getVotersPublicKeyss() public view returns (RsaPublicKey[] memory) {
        return votersPublicKeys;
    }

    function getVotersPublicKey(uint id) public view returns (RsaPublicKey memory) {
        return votersPublicKeys[id];
    }

    struct PaillierPublicKey {
        string n_base64;
        string g_base64;
    }

    struct RsaPublicKey {
        string modulus_base64;
        string exponent_base64;
    }

    struct RingSignature {
//        keys: List<PublicKey>,
        string startValue;
        string[] ringValues;
        string tag;
    }

    struct Vote {
        string[] voteContent;
        string[] voterEncryptedAnswers;
        RingSignature ringSignature;
    }

    function validateRsaRingSignature(RingSignature memory ringSignature, string memory signedMessage) private {
        BigNumber memory hash = cryptoHash(signedMessage);
        logger.push("BASE64:");
        logger.push(Base64.encode(hash.val, true, true));
        logger.push(Base64.encode(hash.val, true, false));
        logger.push(Base64.encode(hash.val, false, true));
        logger.push(Base64.encode(hash.val, true, true));
        bytes memory testBytes = xorByteArrays(hash.val, hash.val);
        uint ringSize = ringSignature.ringValues.length;
        logger.push("RING VERIFICATION");
        for (uint i = 0; i < ringSize; i++) {
            logger.push("RING VALUE");
            logger.push(iToHex2(Base64.decode(ringSignature.ringValues[i])));
//            BigNumber memory ringValue = BigNumbers.init(ringSignature.ringValues[i], false);
//            hash = hash.add(ringValue);
        }
    }

    function xorByteArrays(bytes memory arr1, bytes memory arr2) public pure returns (bytes memory) {
        require(arr1.length == arr2.length, "Arrays must be of equal length");

        bytes memory result = new bytes(arr1.length);

        for (uint256 i = 0; i < arr1.length; i++) {
            result[i] = arr1[i] ^ arr2[i];
        }

        return result;
    }

    function cryptoHash(string memory message) private returns (BigNumber memory) {
        logger.push("bytes");
        logger.push(message);
        logger.push(Base64.encode(bytes(message), false, false));
        logger.push(iToHex(sha256(bytes(message))));
        logger.push(iToHex(sha256(bytes(message))));
        logger.push(iToHex2(abi.encodePacked(sha256(bytes(message)))));
        bytes memory hashBytes = abi.encodePacked(sha256(bytes(message)));
        logger.push(Base64.encode(hashBytes, false, false));
        return BigNumbers.init(hashBytes, false);
    }

    function iToHex2(bytes memory buffer) public pure returns (string memory) {

        // Fixed buffer size for hexadecimal convertion
        bytes memory converted = new bytes(buffer.length * 2);

        bytes memory _base = "0123456789abcdef";

        for (uint256 i = 0; i < buffer.length; i++) {
            converted[i * 2] = _base[uint8(buffer[i]) / _base.length];
            converted[i * 2 + 1] = _base[uint8(buffer[i]) % _base.length];
        }

        return string(abi.encodePacked("0x", converted));
    }


    function iToHex(bytes32 buffer) public pure returns (string memory) {

        // Fixed buffer size for hexadecimal convertion
        bytes memory converted = new bytes(buffer.length * 2);

        bytes memory _base = "0123456789abcdef";

        for (uint256 i = 0; i < buffer.length; i++) {
            converted[i * 2] = _base[uint8(buffer[i]) / _base.length];
            converted[i * 2 + 1] = _base[uint8(buffer[i]) % _base.length];
        }

        return string(abi.encodePacked("0x", converted));
    }

//    function addVote(Vote memory vote) public {
//        votes.push(vote);
//    }
//
//    function getVotes() public view returns (Vote[] memory) {
//        return votes;
//    }

    function testBigNumbers() public {
    }

    function vote(Vote memory newVote) public {
        require(newVote.voteContent.length == candidates.length, "Invalid vote content length");
        require(newVote.voterEncryptedAnswers.length == voterQuestions.length, "Invalid answers length");
//        validateRsaRingSignature(newVote.ringSignature, newVote.candidate);
        votes.push(newVote);
    }

    function getLogs() public view returns (string[] memory){
        return logger;
    }

    function clearLogs() public {
        logger = new string[](0);
    }

}