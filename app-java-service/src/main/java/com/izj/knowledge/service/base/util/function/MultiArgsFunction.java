package com.izj.knowledge.service.base.util.function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiArgsFunction {

    @FunctionalInterface
    public interface Consumer4Args<Arg1, Arg2, Arg3, Arg4> {
        public void apply(Arg1 arg1, Arg2 arg2, Arg3 arg3, Arg4 arg4);
    }
}
