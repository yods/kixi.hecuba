# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "kixi-dev"

  config.vm.box_url = "http://mc-deployments-public.s3-website-us-west-2.amazonaws.com/kixi-dev.box"

  config.vm.provider :virtualbox do |vb, override|
    # headless mode
    vb.gui = false

    # Use VBoxManage to customize the VM.
    # See http://www.virtualbox.org/manual/ch08.html#idp56624480
    vb.customize ["modifyvm", :id, "--memory", "1024"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
    override.vm.network :forwarded_port, guest: 9092, host: 9092 #Kafka
    override.vm.network :forwarded_port, guest: 2181, host: 2181 #Zookeeper
    override.vm.network :forwarded_port, guest: 9042, host: 9042 #Cassandra CQL
    override.vm.network :forwarded_port, guest: 9160, host: 9160 #Cassandra Thrift
    override.vm.network :forwarded_port, guest: 7199, host: 7199 #Cassandra JMX (TODO - confirm)
 
  end

end