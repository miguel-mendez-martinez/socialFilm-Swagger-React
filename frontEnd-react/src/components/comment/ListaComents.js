import { AVISO } from "../todo/indexAviso"
import Estrella from './icons/estrellaEntera.png'
import EstrellaV from './icons/estrellaVacia.png'
import { Link } from '../../components'
import {  useSearchUsers } from "../../hooks"

const picture = user => {
    if(user.content){
        const picture = user.content[0]?.picture === 'none' ? "https://media.istockphoto.com/id/517998264/es/vector/hombre-icono-de-usuario.jpg?s=612x612&w=is&k=20&c=qX2mEhSna0SxcxMXXyS8YZRaYBm8itZ9N9Ma1S4Bug4=" : user.content[0].picture

        return picture
    }else{
        return null
    }
}

export function ListaComents({comments = [], movie}) {
    const { content = [] } = comments

    return <div className="overflow-x-scroll">
        {content.length === 0 ? <AVISO>No se han publicado comentarios sobre esta pelicula.</AVISO> : 
        <div className="min-w-min" /* style={{ width: "180rem" }} */>
            <div className = 'p-5 flex flex-nowrap  space-x-12'  style={{ height: '26rem' }} >
                        {
                            content?.map((comment, idx) =>
                                <Element key = { comment.id } comment = {comment} movie = { movie } total = { content.length }/>
                            )
                        }
            </div>
        </div>}
    </div>
}

function Element({comment, className = '', active, movie, total}) {
    const estrellas = [1,2,3,4,5,6,7,8,9,10]
    const {user, search} = useSearchUsers(comment.user.email)

    return <>{movie?.id === undefined ? null : 
        <div className = 'h-full min-w-min flex flex-col space-y-2 p-5 border rounded-md' style = {{boxShadow: '3px 5px rgb(0 0 0 / 0.15)', width: "55rem"}}>
            <div className="flex justify-center items-center" >
                <Link variant = 'plain-secondary' className = 'mb-2 flex-1 flex' to = {`/profile/${comment.user.email}`}>
                    {movie.id === comment.movie.id ? <img style = {{ height: "25px" }}
                            src = { picture(user) }
                            alt = { `${ user?.email } poster` }
                            className = 'rounded-full z-20' /> : null}
                    <div className="flex-1 font-bold ml-2">
                        <span className="">{comment.user.email}</span>
                    </div>
                </Link>
                <div className="grow">
                    <div className="flex" style={{ height: '25px' }}>
                            {estrellas.map((estrella, idx) => 
                                <img src = {idx+1 <= comment.rating ? Estrella : EstrellaV}
                                id = {idx+1}
                                alt = { `estrella ${idx}` }
                                key = {idx}
                                style = {{ width: '25px' }}/>
                            )}
                    </div>
                </div>
            </div>
            <div className="font-serif">{comment.comment}</div>
        </div>}</>
}