from typing import TypeVar, Generic, Callable

T = TypeVar('T')


class InstanceManager(Generic[T]):
    """
    Manages __instances of a specific class [T].

    Args:
        factory (callable[T]): A function that creates a new instance of the class.
    """

    def __init__(self, factory: Callable[[], T]):
        self.__instances = {}
        self.factory = factory

    def __delitem__(self, key: str) -> T:
        if key in self.__instances:
            return self.__instances.pop(key)

    def __getitem__(self, key: str) -> T:
        try_get = self.__instances.get(key)
        if try_get is None:
            instance = self.factory()
            self.__instances[key] = instance
            return instance
        return try_get

    def __setitem__(self, key, value: T):
        self.__instances[key] = value
        pass
