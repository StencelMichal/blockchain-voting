// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

    enum VotingPhase {INITIALIZATION, VOTING, TALLYING}

    struct VotingResult {
        uint totalVotes;
        uint[] votes;
    }

    struct EncodedVote {
        string ciphertext;
        string exponent;
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
        string startValue;
        string[] moduluses_base64;
        string[] exponents_base64;
        string[] ringValues;
        string tag;
    }

    struct Vote {
        string[] encryptedVotes;
        uint[] encryptedExponents;
        string[] voterEncryptedAnswers;
        RingSignature ringSignature;
    }
