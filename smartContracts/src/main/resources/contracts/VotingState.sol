// SPDX-License-Identifier: MIT
pragma solidity 0.8.17;

import {BigNumbers, BigNumber} from "./BigNumbers.sol";
import "Base64.sol";
import "Entities.sol";

contract VotingState {

    // Configuration
    PaillierPublicKey public commonEncryptionKey;
    VotingPhase public state;
    string[] public voterQuestions;
    string[] public candidates;
    RsaPublicKey[] public votersPublicKeys;

    // Voting
    string[] public awaitingValidationVotesTags;
    mapping(string => Vote) public awaitingValidationVotesQueue;
    Vote[] public validatedVotes;
    Vote[] public invalidVotes;

    // Result
    VotingResult public votingResult;

    address private bootnodeAddress;

    string[] public logger;

    constructor(address _bootnodeAddress) {
        state = VotingPhase.INITIALIZATION;
        bootnodeAddress = _bootnodeAddress;
    }

    // INITIALIZATION

    function initializeVoting(
        PaillierPublicKey memory key,
        string[] memory _candidates,
        string[] memory _voterQuestions,
        RsaPublicKey[] memory _votersPublicKeys
    ) onlyBootnode onInitialization public {
        // TODO assert bootnode
        commonEncryptionKey = key;
        candidates = _candidates;
        voterQuestions = _voterQuestions;
        for (uint i = 0; i < _votersPublicKeys.length; i++) {
            votersPublicKeys.push(_votersPublicKeys[i]);
        }
        state = VotingPhase.VOTING;
    }

    // VOTING

    function getCandidates() onVotingOrTallying public view returns (string[] memory) {
        return candidates;
    }

    function getVoterQuestions() onVotingOrTallying public view returns (string[] memory) {
        return voterQuestions;
    }

    function vote(Vote memory newVote) onVoting public {
        require(newVote.encryptedVotes.length == candidates.length, "Invalid vote content length");
        require(newVote.voterEncryptedAnswers.length == voterQuestions.length, "Invalid answers length");
        require(newVote.encryptedVotes.length == newVote.encryptedExponents.length, "Invalid encrypted votes length");
        string memory tag = newVote.ringSignature.tag;
        awaitingValidationVotesTags.push(tag);
        awaitingValidationVotesQueue[tag] = newVote;
    }

    function getVoteAwaitingForValidation() onVoting public view returns (Vote memory) {
        string memory tag = awaitingValidationVotesTags[0];
        return awaitingValidationVotesQueue[tag];
    }

    function confirmVoteValidity(Vote memory validatedVote) onVoting public {
        //TODO add sealerAddress
        validatedVotes.push(validatedVote);
        removeVoteFromQueue(validatedVote.ringSignature.tag);
    }

    function invalidateVote(Vote memory invalidVote) onVoting public {
        invalidVotes.push(invalidVote);
        removeVoteFromQueue(invalidVote.ringSignature.tag);
    }

    function changeStateToTallying() onVoting onlyBootnode public {
        state = VotingPhase.TALLYING;
    }

    function removeVoteFromQueue(string memory tag) onVoting private {
        for (uint i = 0; i < awaitingValidationVotesTags.length; i++) {
            string memory currentTag = awaitingValidationVotesTags[i];
            if (keccak256(bytes(currentTag)) == keccak256(bytes(tag))) {
                awaitingValidationVotesTags[i] = awaitingValidationVotesTags[awaitingValidationVotesTags.length - 1];
                awaitingValidationVotesTags.pop();
                return;
            }
        }
    }

    // TALLYING

    function publishVotingResults(VotingResult memory result) public onlyBootnode onTallying {
        // TODO assert
        votingResult = result;
    }

    function retrieveVotingResults() public view onTallying returns (VotingResult memory) {
        return votingResult;
    }

    function getAllVotes() public view onTallying returns (Vote[] memory) {
        return validatedVotes;
    }

    // UTILS

    modifier onlyBootnode() {
        require(msg.sender == bootnodeAddress, "Only bootnode can call this function");
        _;
    }

    modifier onInitialization() {
        require(state == VotingPhase.INITIALIZATION, "This funtion may be called only on initialization");
        _;
    }

    modifier onVoting() {
        require(state == VotingPhase.VOTING, "This funtion may be called only on voting");
        _;
    }

    modifier onTallying() {
        require(state == VotingPhase.TALLYING, "This funtion may be called only on tallying");
        _;
    }

    modifier onVotingOrTallying() {
        require(state == VotingPhase.VOTING || state == VotingPhase.TALLYING, "This funtion may be called only on voting");
        _;
    }

//    function validateRsaRingSignature(RingSignature memory ringSignature, string memory signedMessage) private {
//        BigNumber memory hash = cryptoHash(signedMessage);
//        logger.push("BASE64:");
//        logger.push(Base64.encode(hash.val, true, true));
//        logger.push(Base64.encode(hash.val, true, false));
//        logger.push(Base64.encode(hash.val, false, true));
//        logger.push(Base64.encode(hash.val, true, true));
//        bytes memory testBytes = xorByteArrays(hash.val, hash.val);
//        uint ringSize = ringSignature.ringValues.length;
//        logger.push("RING VERIFICATION");
//        for (uint i = 0; i < ringSize; i++) {
//            logger.push("RING VALUE");
//            logger.push(iToHex2(Base64.decode(ringSignature.ringValues[i])));
////            BigNumber memory ringValue = BigNumbers.init(ringSignature.ringValues[i], false);
////            hash = hash.add(ringValue);
//        }
//    }

//    function xorByteArrays(bytes memory arr1, bytes memory arr2) public pure returns (bytes memory) {
//        require(arr1.length == arr2.length, "Arrays must be of equal length");
//
//        bytes memory result = new bytes(arr1.length);
//
//        for (uint256 i = 0; i < arr1.length; i++) {
//            result[i] = arr1[i] ^ arr2[i];
//        }
//
//        return result;
//    }

//    function cryptoHash(string memory message) private returns (BigNumber memory) {
//        logger.push("bytes");
//        logger.push(message);
//        logger.push(Base64.encode(bytes(message), false, false));
//        logger.push(iToHex(sha256(bytes(message))));
//        logger.push(iToHex(sha256(bytes(message))));
//        logger.push(iToHex2(abi.encodePacked(sha256(bytes(message)))));
//        bytes memory hashBytes = abi.encodePacked(sha256(bytes(message)));
//        logger.push(Base64.encode(hashBytes, false, false));
//        return BigNumbers.init(hashBytes, false);
//    }

//    function iToHex2(bytes memory buffer) public pure returns (string memory) {
//
//        // Fixed buffer size for hexadecimal convertion
//        bytes memory converted = new bytes(buffer.length * 2);
//
//        bytes memory _base = "0123456789abcdef";
//
//        for (uint256 i = 0; i < buffer.length; i++) {
//            converted[i * 2] = _base[uint8(buffer[i]) / _base.length];
//            converted[i * 2 + 1] = _base[uint8(buffer[i]) % _base.length];
//        }
//
//        return string(abi.encodePacked("0x", converted));
//    }

//    function iToHex(bytes32 buffer) public pure returns (string memory) {
//
//        // Fixed buffer size for hexadecimal convertion
//        bytes memory converted = new bytes(buffer.length * 2);
//
//        bytes memory _base = "0123456789abcdef";
//
//        for (uint256 i = 0; i < buffer.length; i++) {
//            converted[i * 2] = _base[uint8(buffer[i]) / _base.length];
//            converted[i * 2 + 1] = _base[uint8(buffer[i]) % _base.length];
//        }
//
//        return string(abi.encodePacked("0x", converted));
//    }

//    function addVote(Vote memory vote) public {
//        votes.push(vote);
//    }
//
//    function getVotes() public view returns (Vote[] memory) {
//        return votes;
//    }

//    function getLogs() public view returns (string[] memory){
//        return logger;
//    }

//    function clearLogs() public {
//        logger = new string[](0);
//    }

}