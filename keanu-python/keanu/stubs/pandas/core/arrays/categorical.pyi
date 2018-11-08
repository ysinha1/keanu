# Stubs for pandas.core.arrays.categorical (Python 3.6)
#
# NOTE: This dynamically typed stub was automatically generated by stubgen.

from .base import ExtensionArray
from pandas.core.accessor import PandasDelegate
from pandas.core.base import NoNewAttributesMixin, PandasObject
from typing import Any, Optional

def contains(cat: Any, key: Any, container: Any): ...

class Categorical(ExtensionArray, PandasObject):
    __array_priority__: int = ...
    def __init__(self, values: Any, categories: Optional[Any] = ..., ordered: Optional[Any] = ..., dtype: Optional[Any] = ..., fastpath: bool = ...) -> None: ...
    @property
    def categories(self): ...
    @categories.setter
    def categories(self, categories: Any) -> None: ...
    @property
    def ordered(self): ...
    @property
    def dtype(self): ...
    def copy(self): ...
    def astype(self, dtype: Any, copy: bool = ...): ...
    def ndim(self): ...
    def size(self): ...
    def itemsize(self): ...
    def tolist(self): ...
    @property
    def base(self): ...
    @classmethod
    def from_codes(cls, codes: Any, categories: Any, ordered: bool = ...): ...
    codes: Any = ...
    def set_ordered(self, value: Any, inplace: bool = ...): ...
    def as_ordered(self, inplace: bool = ...): ...
    def as_unordered(self, inplace: bool = ...): ...
    def set_categories(self, new_categories: Any, ordered: Optional[Any] = ..., rename: bool = ..., inplace: bool = ...): ...
    def rename_categories(self, new_categories: Any, inplace: bool = ...): ...
    def reorder_categories(self, new_categories: Any, ordered: Optional[Any] = ..., inplace: bool = ...): ...
    def add_categories(self, new_categories: Any, inplace: bool = ...): ...
    def remove_categories(self, removals: Any, inplace: bool = ...): ...
    def remove_unused_categories(self, inplace: bool = ...): ...
    def map(self, mapper: Any): ...
    __eq__: Any = ...
    __ne__: Any = ...
    __lt__: Any = ...
    __gt__: Any = ...
    __le__: Any = ...
    __ge__: Any = ...
    @property
    def shape(self): ...
    def shift(self, periods: Any): ...
    def __array__(self, dtype: Optional[Any] = ...): ...
    @property
    def T(self): ...
    @property
    def nbytes(self): ...
    def memory_usage(self, deep: bool = ...): ...
    def searchsorted(self, value: Any, side: str = ..., sorter: Optional[Any] = ...): ...
    def isna(self): ...
    isnull: Any = ...
    def notna(self): ...
    notnull: Any = ...
    def put(self, *args: Any, **kwargs: Any) -> None: ...
    def dropna(self): ...
    def value_counts(self, dropna: bool = ...): ...
    def get_values(self): ...
    def check_for_ordered(self, op: Any) -> None: ...
    def argsort(self, *args: Any, **kwargs: Any): ...
    def sort_values(self, inplace: bool = ..., ascending: bool = ..., na_position: str = ...): ...
    def ravel(self, order: str = ...): ...
    def view(self): ...
    def to_dense(self): ...
    def fillna(self, value: Optional[Any] = ..., method: Optional[Any] = ..., limit: Optional[Any] = ...): ...
    def take_nd(self, indexer: Any, allow_fill: Optional[Any] = ..., fill_value: Optional[Any] = ...): ...
    take: Any = ...
    def __len__(self): ...
    def __iter__(self): ...
    def __contains__(self, key: Any): ...
    def __unicode__(self): ...
    def __getitem__(self, key: Any): ...
    def __setitem__(self, key: Any, value: Any) -> None: ...
    def min(self, numeric_only: Optional[Any] = ..., **kwargs: Any): ...
    def max(self, numeric_only: Optional[Any] = ..., **kwargs: Any): ...
    def mode(self, dropna: bool = ...): ...
    def unique(self): ...
    def equals(self, other: Any): ...
    def is_dtype_equal(self, other: Any): ...
    def describe(self): ...
    def repeat(self, repeats: Any, *args: Any, **kwargs: Any): ...
    def isin(self, values: Any): ...

class CategoricalAccessor(PandasDelegate, PandasObject, NoNewAttributesMixin):
    index: Any = ...
    name: Any = ...
    def __init__(self, data: Any) -> None: ...
    @property
    def codes(self): ...
