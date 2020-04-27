#!/bin/bash

set -eu -o pipefail

sudo add-apt-repository -y ppa:ethereum/ethereum
sudo apt-get update -y
sudo apt-get install -y git python2.7 make g++ python-pip solc
sudo pip install --upgrade pip

curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.34.0/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"
nvm install 8.15.0
nvm use 8.15.0
npm config set python python2.7
npm install -g truffle ganache-cli

echo done

# done!
echo
echo 'The vagrant instance has been provisioned.'
echo "Use 'vagrant ssh' to open a terminal, 'vagrant suspend' to stop the instance, and 'vagrant destroy' to remove it."