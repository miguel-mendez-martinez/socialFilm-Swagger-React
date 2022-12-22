
getMovies
/* 
        {
            filter: { genre = '', title = '', status = '' } = { genre : '', title : '', status : '' },
            sort,
            pagination: {page = 0, size = 7} = { page: 0, size: 7 }
        } = {
            filter: { genre : '', title : '', status : '' },
            sort: {},
            pagination: { page: 0, size: 7 }
        }
*/
getMovies
/* return new Promise(resolve => {

            const filtered = DATA.movies
                ?.filter(movie => movie.title.toLowerCase().includes(title.toLowerCase() || ''))
                ?.filter(movie => genre !== '' ? movie.genres.map(genre => genre.toLowerCase()).includes(genre.toLowerCase()) : true)
                ?.filter(movie => movie.status.toLowerCase().includes(status.toLowerCase() || ''))

            const data = {
                content: filtered?.slice(size * page, size * page + size),
                pagination: {
                    hasNext: size * page + size < filtered.length,
                    hasPrevious: page > 0
                }
            }

            resolve(data)
        }) */


carruselCom
/* const [{activeIndex}, dispatch] = useReducer(
    (state, action) => {
        switch(action.type) {
            case 'next': return { activeIndex: ((state.activeIndex + 1) % content.length) }
            case 'prev': return { activeIndex: ((state.activeIndex - 1) % content.length) }
            case 'pause': return { activeIndex: state.activeIndex }
            case 'set': return  { activeIndex: action.payload }
            default: throw new Error('Invalid action')
        }
    },
    { activeIndex: 0 },
    () => ({ activeIndex: 0 })
) 
<Element key = { comment.id } comment = {comment} active = { activeIndex } index = { idx } total = { content.length }/>*/