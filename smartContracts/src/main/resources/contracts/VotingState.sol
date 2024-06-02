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
    mapping(address => bool) private sealersAddresses;
    mapping(address => bool) private votersAddresses;

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
        RsaPublicKey[] memory _votersPublicKeys,
        address[] memory _sealersAddresses,
        address[] memory _votersAddresses
    ) onlyBootnode onInitialization public {
        commonEncryptionKey = key;
        candidates = _candidates;
        voterQuestions = _voterQuestions;
        for (uint i = 0; i < _votersPublicKeys.length; i++) {
            votersPublicKeys.push(_votersPublicKeys[i]);
        }
        for (uint i = 0; i < _sealersAddresses.length; i++) {
            sealersAddresses[_sealersAddresses[i]] = true;
        }
        for (uint i = 0; i < _votersAddresses.length; i++) {
            votersAddresses[_votersAddresses[i]] = true;
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

    function vote(Vote memory newVote) onVoting onlyVoter public {
        require(newVote.encryptedVotes.length == candidates.length, "Invalid vote content length");
        require(newVote.voterEncryptedAnswers.length == voterQuestions.length, "Invalid answers length");
        require(newVote.encryptedVotes.length == newVote.encryptedExponents.length, "Invalid encrypted votes length");
        string memory tag = newVote.ringSignature.tag;
        awaitingValidationVotesTags.push(tag);
        awaitingValidationVotesQueue[tag] = newVote;
    }

    function getVoteAwaitingForValidation() onVoting onlySealer public view returns (Vote memory) {
        string memory tag = awaitingValidationVotesTags[0];
        return awaitingValidationVotesQueue[tag];
    }

    function confirmVoteValidity(Vote memory validatedVote) onVoting onlySealer public {
        validatedVotes.push(validatedVote);
        removeVoteFromQueue(validatedVote.ringSignature.tag);
    }

    function invalidateVote(Vote memory invalidVote) onVoting onlySealer public {
        invalidVotes.push(invalidVote);
        removeVoteFromQueue(invalidVote.ringSignature.tag);
    }

    function changeStateToTallying() onVoting onlyBootnode public {
        state = VotingPhase.TALLYING;
    }

    function removeVoteFromQueue(string memory tag) onVoting onlySealer private {
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

    modifier onlySealer() {
        require(sealersAddresses[msg.sender], "Only selaer can call this function");
        _;
    }

    modifier onlyVoter() {
        require(votersAddresses[msg.sender], "Only voter can call this function");
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

}