var MetaCoin = artifacts.require("./MetaCoin.sol");

contract('MetaCoin', function(accounts) {

  it("should put 10000 MetaCoin in the first account", function() {

    return MetaCoin.deployed()
        .then(function(instance) {
            return instance.getBalance.call(accounts[0]);
        }).then(function(balance) {
            assert.equal(balance.valueOf(), 10000, "10000 wasn't in the first account");
    });
  });

  it("should send coin correctly", function() {
    var meta;

    // Get initial balances of first and second account.
    var account_one = accounts[0];
    var account_two = accounts[1];

    var account_one_starting_balance;
    var account_two_starting_balance;
    var account_one_ending_balance;
    var account_two_ending_balance;

    var amount = 10;

    return MetaCoin.deployed().then(function(instance) {
      meta = instance;
      return meta.getBalance.call(account_one);
    }).then(function(balance) {
      account_one_starting_balance = balance.toNumber();
      return meta.getBalance.call(account_two);
    }).then(function(balance) {
      account_two_starting_balance = balance.toNumber();
      return meta.sendCoin(account_two, amount, {from: account_one});
    }).then(function() {
      return meta.getBalance.call(account_one);
    }).then(function(balance) {
      account_one_ending_balance = balance.toNumber();
      return meta.getBalance.call(account_two);
    }).then(function(balance) {
      account_two_ending_balance = balance.toNumber();

      assert.equal(account_one_ending_balance, account_one_starting_balance - amount, "Amount wasn't correctly taken from the sender");
      assert.equal(account_two_ending_balance, account_two_starting_balance + amount, "Amount wasn't correctly sent to the receiver");
    });
  });

  it("should allow owner to mint coins", function()
  {
    //call the mint function on behalf of the owner and check that owner's account balance increased correctly
    var meta;
    var account_owner = accounts[0];
    var amount = 1;
    var account_owner_starting_balance;
    var account_owner_ending_balance;

    return MetaCoin.deployed().then(function (instance)
    {
        meta = instance;
        return meta.getBalance.call(account_owner);
    }).then(function (balance)
    {
        account_owner_starting_balance = balance.toNumber();
        return meta.mint(amount);
    }).then(function ()
    {
        return meta.getBalance.call(account_owner);
    }).then(function (balance)
    {
        account_owner_ending_balance = balance.toNumber();
		assert.equal(account_owner_ending_balance, account_owner_starting_balance + amount, "Amount wasn't correctly minted to the owner");
	});
  });

  it("should not allow a non-owner address to mint coins", function() {
    //call the mint function on behalf of a non-owner address and check that the non-owner address balance stays the same
    var meta;
    var account_non_owner = accounts[1];
    var amount = 1;
    var account_non_owner_starting_balance;
    var account_non_owner_ending_balance;

    return MetaCoin.deployed().then(function(instance)
    {
        meta = instance;
        return meta.getBalance.call(account_non_owner);
    }).then(function(balance)
    {
        account_non_owner_starting_balance = balance.toNumber();
        return meta.mint(amount, {from: account_non_owner});
    }).then(function()
    {
        return meta.getBalance.call(account_non_owner);
    }).then(function(balance)
    {
        account_non_owner_ending_balance = balance.toNumber();
    	assert.equal(account_non_owner_starting_balance, account_non_owner_ending_balance, "Non-owner allowed to mint coins");
    });
  });
});
