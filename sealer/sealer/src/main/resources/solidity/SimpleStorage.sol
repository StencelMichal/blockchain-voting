pragma solidity ^0.8.19;

contract VotingState {
  string private number;

  function setNumber(string memory _name) public {
    number = _name;
  }

  function getNumber() public view returns (string memory) {
    return number;
  }

}