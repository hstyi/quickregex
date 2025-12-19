# QuickRegEx

QuickRegEx is an extraction of the regular expression module from QuickJS, enabling developers to easily integrate it into their projects.

## How to Use?

You need to download the [latest version](https://github.com/hstyi/quick-regex/releases/latest) and then include `quickregex/quickregex.h` in your project.

### CMakeLists.txt

```CMake
cmake_minimum_required(VERSION 4.1)
project(test C)
set(CMAKE_C_STANDARD 11)
add_executable(test quickregex/quickregex.c main.c)
```

### main.c

```C
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "quickregex/quickregex.h"

int main(void) {
    {
        const char *regex = "^Hello";
        char error_msg[64];
        int len = 0;
        uint8_t *bc = lre_compile(&len, error_msg, sizeof(error_msg), regex, strlen(regex), 0, NULL);
        assert(bc != NULL);

        const char *text = "Hello World!";
        uint8_t *capture[255 * 2];
        // like javascript /^Hello/.test('Hello World!')
        assert(lre_exec(capture, bc, (const uint8_t *) text, 0, (int) strlen(text), 0, NULL) == 1);
        assert(lre_exec(capture, bc, (const uint8_t *) "text", 0, 4, 0, NULL) != 1);

        free(bc);
    }

    {
        const char *regex = "^(He)llo(.*)";
        char error_msg[64];
        int len = 0;
        uint8_t *bc = lre_compile(&len, error_msg, sizeof(error_msg), regex, strlen(regex), 0, NULL);
        assert(bc != NULL);

        const char *text = "Hello World!";
        uint8_t *capture[255 * 2];
        // like javascript /^(He)llo(.*)/.exec('Hello World!')
        if (lre_exec(capture, bc, (const uint8_t *) text, 0, (int) strlen(text), 0, NULL) == 1) {
            const int count = lre_get_capture_count(bc);
            for (int i = 0; i < count; i++) {
                const int length = (int) (capture[2 * i + 1] - capture[2 * i]);
                printf("Group.%i: %i\n", i, length);
            }
        }

        free(bc);
    }

    return 0;
}
```

### Single-file

If you are not using CMakeLists, you can directly include the `quickregex/quickregex.c` file.

```C
#include "quickregex/quickregex.c"

int main(void) {
    // your code here
    return 0;
}
```
