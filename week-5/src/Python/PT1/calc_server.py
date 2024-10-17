import rpyc
from rpyc.utils.server import ThreadedServer

@rpyc.service
class TestService(rpyc.Service):
    @rpyc.exposed
    class Calc():
        @rpyc.exposed
        def add(a, b):
            return a+b

        @rpyc.exposed
        def sub(a, b):
            return a - b

        @rpyc.exposed
        def tolower(s):
            return s.lower()

print('starting server')
server = ThreadedServer(TestService, port=12345)
server.start()

