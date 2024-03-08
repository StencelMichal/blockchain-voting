pragma solidity ^0.8.19;

contract VotingState {

    Vote[] public votes;

    struct Vote {
        string candidate;
    }

    function addVote(Vote memory vote) public {
        votes.push(vote);
    }

    function getVotes() public view returns (Vote[] memory) {
        return votes;
    }

}