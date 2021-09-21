Найти коммит, в котором в репозиторий интерпретатора CPython был добавлен модуль 
для бинарного поиска (bisect). Для этого предлагается реализовать собственный 
упрощённый аналог git bisect.

Интерфейс:
> git-bisect <last-commit-in-old-state> <first-commit-in-new-state> <command ...>


первый тег – 7f777ed95a19224294949e1b4ce56bbffcb1fe9f
первый коммит – 7f777ed95a19224294949e1b4ce56bbffcb1fe9f 