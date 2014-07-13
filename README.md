# cloft2

    $ -- Prepare Server Plugin
    $ cd server
    $ lein uberjar
    $ cp -p ./run ~/src/craftbukkit
    $ ln -s ./target/sugoi.jar ~/src/craftbukkit/plugins/
    # -- Start server
    $ cd ~/src/craftbukkit
    $ iexe ./run
    # -- Inject client
    $ cd -
    $ cd ../client
    $ lein run

By default cloft2 server/client uses port 7888 but you can configure (eventually.)

### Might be Useful

* <http://jenkins.raa0121.info/job/cloft2/>
* <http://lingr.com/room/mcujm>

## License

Copyright (c) 2014 Tatsuhiro Ujihisa

Distributed under the GPL version 3 or any later version.
