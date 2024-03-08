package com.stencel.evoting.sealer;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.5.2.
 */
@SuppressWarnings("rawtypes")
public class VotingState extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b5061060f806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c80630dc96015146100465780632594b00b146100645780635df8133014610079575b600080fd5b61004e610099565b60405161005b91906102c0565b60405180910390f35b61007761007236600461039a565b610186565b005b61008c61008736600461045d565b6101cc565b60405161005b9190610476565b60606000805480602002602001604051908101604052809291908181526020016000905b8282101561017d578382906000526020600020016040518060200160405290816000820180546100ec90610490565b80601f016020809104026020016040519081016040528092919081815260200182805461011890610490565b80156101655780601f1061013a57610100808354040283529160200191610165565b820191906000526020600020905b81548152906001019060200180831161014857829003601f168201915b505050505081525050815260200190600101906100bd565b50505050905090565b60008054600181018255908052815182917f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e563019081906101c69082610519565b50505050565b600081815481106101dc57600080fd5b6000918252602090912001805490915081906101f790610490565b80601f016020809104026020016040519081016040528092919081815260200182805461022390610490565b80156102705780601f1061024557610100808354040283529160200191610270565b820191906000526020600020905b81548152906001019060200180831161025357829003601f168201915b5050505050905081565b6000815180845260005b818110156102a057602081850181015186830182015201610284565b506000602082860101526020601f19601f83011685010191505092915050565b6000602080830181845280855180835260408601915060408160051b870101925083870160005b8281101561031d57878503603f1901845281515186865261030a8787018261027a565b95505092850192908501906001016102e7565b5092979650505050505050565b634e487b7160e01b600052604160045260246000fd5b6040516020810167ffffffffffffffff811182821017156103635761036361032a565b60405290565b604051601f8201601f1916810167ffffffffffffffff811182821017156103925761039261032a565b604052919050565b600060208083850312156103ad57600080fd5b823567ffffffffffffffff808211156103c557600080fd5b81850191508282870312156103d957600080fd5b6103e1610340565b8235828111156103f057600080fd5b80840193505086601f84011261040557600080fd5b8235828111156104175761041761032a565b610429601f8201601f19168601610369565b9250808352878582860101111561043f57600080fd5b80858501868501376000908301909401939093528252509392505050565b60006020828403121561046f57600080fd5b5035919050565b602081526000610489602083018461027a565b9392505050565b600181811c908216806104a457607f821691505b6020821081036104c457634e487b7160e01b600052602260045260246000fd5b50919050565b601f82111561051457600081815260208120601f850160051c810160208610156104f15750805b601f850160051c820191505b81811015610510578281556001016104fd565b5050505b505050565b815167ffffffffffffffff8111156105335761053361032a565b610547816105418454610490565b846104ca565b602080601f83116001811461057c57600084156105645750858301515b600019600386901b1c1916600185901b178555610510565b600085815260208120601f198616915b828110156105ab5788860151825594840194600190910190840161058c565b50858210156105c95787850151600019600388901b60f8161c191681555b5050505050600190811b0190555056fea2646970667358221220b415f5c6f516b7fb417f98d4ff5723aee4c913a3d1e75f479636572044cc2bd664736f6c63430008130033";

    public static final String FUNC_ADDVOTE = "addVote";

    public static final String FUNC_GETVOTES = "getVotes";

    public static final String FUNC_VOTES = "votes";

    @Deprecated
    protected VotingState(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected VotingState(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected VotingState(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected VotingState(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> addVote(Vote vote) {
        final Function function = new Function(
                FUNC_ADDVOTE, 
                Arrays.<Type>asList(vote), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List> getVotes() {
        final Function function = new Function(FUNC_GETVOTES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Vote>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<String> votes(BigInteger param0) {
        final Function function = new Function(FUNC_VOTES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    @Deprecated
    public static VotingState load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new VotingState(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static VotingState load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new VotingState(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static VotingState load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new VotingState(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static VotingState load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new VotingState(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<VotingState> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(VotingState.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<VotingState> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(VotingState.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<VotingState> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(VotingState.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<VotingState> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(VotingState.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class Vote extends DynamicStruct {
        public String candidate;

        public Vote(String candidate) {
            super(new org.web3j.abi.datatypes.Utf8String(candidate));
            this.candidate = candidate;
        }

        public Vote(Utf8String candidate) {
            super(candidate);
            this.candidate = candidate.getValue();
        }
    }
}