package com.ximad.prism.core.model;

public sealed interface BizType permits BizType.Job, BizType.House, BizType.Car, BizType.Ad {
    record Job() implements BizType {}
    record House() implements BizType {}
    record Car() implements BizType {}
    record Ad() implements BizType {}
}
